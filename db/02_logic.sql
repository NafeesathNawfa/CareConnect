-- =====================================================
-- Views, triggers, stored procedures
-- =====================================================
USE free_clinic;

-- ================= VIEWS =================
CREATE OR REPLACE VIEW v_available_slots AS
SELECT s.slot_id, cd.clinic_date, s.start_time, s.end_time,
       d.doctor_id, d.full_name AS doctor_name, sp.name AS specialization
FROM slot s
JOIN clinic_day cd     ON cd.clinic_day_id = s.clinic_day_id
JOIN doctor d          ON d.doctor_id = s.doctor_id
JOIN specialization sp ON sp.specialization_id = d.specialization_id
WHERE s.status = 'AVAILABLE'
  AND cd.is_open = TRUE
  AND d.is_active = TRUE
  AND TIMESTAMP(cd.clinic_date, s.start_time) > NOW();

CREATE OR REPLACE VIEW v_appointment_details AS
SELECT a.appointment_id, a.status, a.reason,
       p.patient_id, p.full_name AS patient_name, p.phone AS patient_phone,
       d.doctor_id,  d.full_name AS doctor_name,
       cd.clinic_date, s.start_time, s.end_time
FROM appointment a
JOIN patient p     ON p.patient_id = a.patient_id
JOIN slot s        ON s.slot_id = a.slot_id
JOIN clinic_day cd ON cd.clinic_day_id = s.clinic_day_id
JOIN doctor d      ON d.doctor_id = s.doctor_id;

CREATE OR REPLACE VIEW v_todays_schedule AS
SELECT * FROM v_appointment_details
WHERE clinic_date = CURDATE() AND status = 'BOOKED'
ORDER BY start_time;

-- ================= TRIGGERS =================
DELIMITER $$

-- New appointment => mark slot BOOKED
CREATE TRIGGER trg_appt_after_insert
AFTER INSERT ON appointment
FOR EACH ROW
BEGIN
    UPDATE slot SET status = 'BOOKED' WHERE slot_id = NEW.slot_id;
    INSERT INTO appointment_log(appointment_id, old_status, new_status)
    VALUES (NEW.appointment_id, NULL, NEW.status);
END$$

-- Status change => log it, and free the slot if cancelled
CREATE TRIGGER trg_appt_after_update
AFTER UPDATE ON appointment
FOR EACH ROW
BEGIN
    IF OLD.status <> NEW.status THEN
        INSERT INTO appointment_log(appointment_id, old_status, new_status)
        VALUES (NEW.appointment_id, OLD.status, NEW.status);

        IF NEW.status = 'CANCELLED' THEN
            UPDATE slot SET status = 'AVAILABLE' WHERE slot_id = NEW.slot_id;
        END IF;
    END IF;
END$$

-- Block illegal status jumps (e.g. reviving a cancelled/completed appointment)
CREATE TRIGGER trg_appt_before_update
BEFORE UPDATE ON appointment
FOR EACH ROW
BEGIN
    IF OLD.status <> 'BOOKED' AND NEW.status <> OLD.status THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Only BOOKED appointments can change status';
    END IF;
END$$

-- ================= STORED PROCEDURES =================

-- Register a patient, returns new id
CREATE PROCEDURE sp_register_patient(
    IN  p_name    VARCHAR(100),
    IN  p_dob     DATE,
    IN  p_gender  VARCHAR(10),
    IN  p_phone   VARCHAR(15),
    IN  p_email   VARCHAR(100),
    IN  p_address VARCHAR(255),
    OUT p_patient_id INT
)
BEGIN
    IF p_dob > CURDATE() THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Date of birth cannot be in the future';
    END IF;
    INSERT INTO patient(full_name, dob, gender, phone, email, address)
    VALUES (p_name, p_dob, p_gender, p_phone, p_email, p_address);
    SET p_patient_id = LAST_INSERT_ID();
END$$

-- Book an appointment (atomic, safe against double booking)
CREATE PROCEDURE sp_book_appointment(
    IN  p_patient_id INT,
    IN  p_slot_id    INT,
    IN  p_reason     VARCHAR(255),
    OUT p_appointment_id INT
)
BEGIN
    DECLARE v_status ENUM('AVAILABLE','BOOKED','BLOCKED');
    DECLARE v_when   DATETIME;
    DECLARE v_open   BOOLEAN;
    DECLARE v_active BOOLEAN;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    -- Lock the slot row so concurrent bookings queue up
    SELECT s.status, TIMESTAMP(cd.clinic_date, s.start_time), cd.is_open, d.is_active
      INTO v_status, v_when, v_open, v_active
      FROM slot s
      JOIN clinic_day cd ON cd.clinic_day_id = s.clinic_day_id
      JOIN doctor d      ON d.doctor_id = s.doctor_id
     WHERE s.slot_id = p_slot_id
       FOR UPDATE;

    IF v_status IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Slot does not exist';
    ELSEIF v_status <> 'AVAILABLE' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Slot is not available';
    ELSEIF v_open = FALSE THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Clinic is closed on that day';
    ELSEIF v_active = FALSE THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Doctor is not active';
    ELSEIF v_when <= NOW() THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cannot book a slot in the past';
    END IF;

    INSERT INTO appointment(patient_id, slot_id, reason)
    VALUES (p_patient_id, p_slot_id, p_reason);
    SET p_appointment_id = LAST_INSERT_ID();

    COMMIT;
END$$

-- Cancel (patient must own the appointment; pass NULL as patient id for staff)
CREATE PROCEDURE sp_cancel_appointment(
    IN p_appointment_id INT,
    IN p_patient_id     INT
)
BEGIN
    DECLARE v_status VARCHAR(20);
    DECLARE v_owner  INT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    SELECT status, patient_id INTO v_status, v_owner
      FROM appointment WHERE appointment_id = p_appointment_id FOR UPDATE;

    IF v_status IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Appointment not found';
    ELSEIF p_patient_id IS NOT NULL AND v_owner <> p_patient_id THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'This appointment belongs to another patient';
    ELSEIF v_status <> 'BOOKED' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Only BOOKED appointments can be cancelled';
    END IF;

    UPDATE appointment SET status = 'CANCELLED' WHERE appointment_id = p_appointment_id;
    COMMIT;
END$$

-- Doctor / receptionist marks outcome: 'COMPLETED' or 'NO_SHOW'
CREATE PROCEDURE sp_set_appointment_outcome(
    IN p_appointment_id INT,
    IN p_outcome        VARCHAR(10)
)
BEGIN
    IF p_outcome NOT IN ('COMPLETED','NO_SHOW') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Outcome must be COMPLETED or NO_SHOW';
    END IF;
    UPDATE appointment SET status = p_outcome WHERE appointment_id = p_appointment_id;
    IF ROW_COUNT() = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Appointment not found or status unchanged';
    END IF;
END$$

-- Admin: create a clinic day (or reopen/close it)
CREATE PROCEDURE sp_set_clinic_day(
    IN p_date    DATE,
    IN p_is_open BOOLEAN,
    IN p_notes   VARCHAR(255)
)
BEGIN
    INSERT INTO clinic_day(clinic_date, is_open, notes)
    VALUES (p_date, p_is_open, p_notes)
    ON DUPLICATE KEY UPDATE is_open = VALUES(is_open), notes = VALUES(notes);
END$$

CREATE PROCEDURE sp_generate_slots(
    IN p_doctor_id     INT,
    IN p_clinic_day_id INT,
    IN p_start         TIME,
    IN p_end           TIME,
    IN p_minutes       INT
)
BEGIN
    DECLARE v_cur  TIME;
    DECLARE v_next TIME;

    IF p_minutes <= 0 OR p_end <= p_start THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid time range or slot length';
    END IF;

    SET v_cur = p_start;
    WHILE ADDTIME(v_cur, SEC_TO_TIME(p_minutes * 60)) <= p_end DO
        SET v_next = ADDTIME(v_cur, SEC_TO_TIME(p_minutes * 60));
        INSERT IGNORE INTO slot(doctor_id, clinic_day_id, start_time, end_time)
        VALUES (p_doctor_id, p_clinic_day_id, v_cur, v_next);
        SET v_cur = v_next;
    END WHILE;
END$$

-- Admin: block / unblock a slot that has no live appointment
CREATE PROCEDURE sp_block_slot(IN p_slot_id INT, IN p_block BOOLEAN)
BEGIN
    IF p_block THEN
        UPDATE slot SET status = 'BLOCKED' WHERE slot_id = p_slot_id AND status = 'AVAILABLE';
    ELSE
        UPDATE slot SET status = 'AVAILABLE' WHERE slot_id = p_slot_id AND status = 'BLOCKED';
    END IF;
    IF ROW_COUNT() = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Slot not found or already booked';
    END IF;
END$$

DELIMITER ;
