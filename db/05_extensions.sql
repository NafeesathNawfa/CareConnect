-- =====================================================
-- Extensions: medical records, medicine inventory, waitlist
-- Run AFTER 01_schema, 02_logic and 03_seed.
-- (This file replaces sp_cancel_appointment from 02 with a
--  waitlist-aware version. Same name, same parameters.)
-- =====================================================
USE free_clinic;

-- ---------- Tables ----------
CREATE TABLE medicine (
    medicine_id   INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(80) NOT NULL UNIQUE,
    unit          VARCHAR(20) NOT NULL DEFAULT 'tablet',
    stock_qty     INT NOT NULL DEFAULT 0,
    reorder_level INT NOT NULL DEFAULT 20,
    CONSTRAINT chk_stock_nonneg CHECK (stock_qty >= 0)
) ENGINE=InnoDB;

-- One visit record per completed appointment
CREATE TABLE visit (
    visit_id       INT AUTO_INCREMENT PRIMARY KEY,
    appointment_id INT NOT NULL UNIQUE,
    diagnosis      VARCHAR(255) NOT NULL,
    notes          TEXT,
    followup_date  DATE,
    recorded_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_visit_appt FOREIGN KEY (appointment_id) REFERENCES appointment(appointment_id)
) ENGINE=InnoDB;

CREATE TABLE prescription (
    prescription_id INT AUTO_INCREMENT PRIMARY KEY,
    visit_id        INT NOT NULL,
    medicine_id     INT NOT NULL,
    quantity        INT NOT NULL,
    instructions    VARCHAR(255),
    CONSTRAINT fk_presc_visit FOREIGN KEY (visit_id)    REFERENCES visit(visit_id),
    CONSTRAINT fk_presc_med   FOREIGN KEY (medicine_id) REFERENCES medicine(medicine_id),
    CONSTRAINT uq_presc UNIQUE (visit_id, medicine_id),
    CONSTRAINT chk_presc_qty CHECK (quantity > 0)
) ENGINE=InnoDB;

-- Every stock change is recorded (restocks and prescriptions)
CREATE TABLE stock_movement (
    movement_id     INT AUTO_INCREMENT PRIMARY KEY,
    medicine_id     INT NOT NULL,
    change_qty      INT NOT NULL,
    reason          ENUM('RESTOCK','PRESCRIPTION') NOT NULL,
    prescription_id INT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_move_med FOREIGN KEY (medicine_id) REFERENCES medicine(medicine_id)
) ENGINE=InnoDB;

-- Patients waiting for a doctor whose day is fully booked
CREATE TABLE waitlist (
    waitlist_id   INT AUTO_INCREMENT PRIMARY KEY,
    patient_id    INT NOT NULL,
    doctor_id     INT NOT NULL,
    clinic_day_id INT NOT NULL,
    status        ENUM('WAITING','BOOKED','REMOVED') NOT NULL DEFAULT 'WAITING',
    joined_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- same trick as appointments: only one WAITING row per patient/doctor/day
    active_key    VARCHAR(40) GENERATED ALWAYS AS
                  (IF(status = 'WAITING', CONCAT_WS('-', patient_id, doctor_id, clinic_day_id), NULL)) VIRTUAL,
    CONSTRAINT fk_wl_patient FOREIGN KEY (patient_id)    REFERENCES patient(patient_id),
    CONSTRAINT fk_wl_doctor  FOREIGN KEY (doctor_id)     REFERENCES doctor(doctor_id),
    CONSTRAINT fk_wl_day     FOREIGN KEY (clinic_day_id) REFERENCES clinic_day(clinic_day_id),
    CONSTRAINT uq_wl_active UNIQUE (active_key)
) ENGINE=InnoDB;

CREATE INDEX idx_wl_queue ON waitlist(doctor_id, clinic_day_id, status, joined_at);

-- ---------- Views ----------
CREATE OR REPLACE VIEW v_patient_history AS
SELECT p.patient_id, p.full_name AS patient_name, cd.clinic_date,
       d.full_name AS doctor_name, v.diagnosis, v.notes, v.followup_date,
       GROUP_CONCAT(CONCAT(m.name, ' x', pr.quantity) SEPARATOR ', ') AS medicines
FROM visit v
JOIN appointment a ON a.appointment_id = v.appointment_id
JOIN patient p     ON p.patient_id = a.patient_id
JOIN slot s        ON s.slot_id = a.slot_id
JOIN clinic_day cd ON cd.clinic_day_id = s.clinic_day_id
JOIN doctor d      ON d.doctor_id = s.doctor_id
LEFT JOIN prescription pr ON pr.visit_id = v.visit_id
LEFT JOIN medicine m      ON m.medicine_id = pr.medicine_id
GROUP BY v.visit_id, p.patient_id, p.full_name, cd.clinic_date,
         d.full_name, v.diagnosis, v.notes, v.followup_date;

CREATE OR REPLACE VIEW v_low_stock AS
SELECT medicine_id, name, unit, stock_qty, reorder_level
FROM medicine
WHERE stock_qty <= reorder_level;

CREATE OR REPLACE VIEW v_waitlist_queue AS
SELECT w.waitlist_id, p.full_name AS patient_name, d.full_name AS doctor_name,
       cd.clinic_date,
       ROW_NUMBER() OVER (PARTITION BY w.doctor_id, w.clinic_day_id
                          ORDER BY w.joined_at, w.waitlist_id) AS position_in_queue
FROM waitlist w
JOIN patient p     ON p.patient_id = w.patient_id
JOIN doctor d      ON d.doctor_id = w.doctor_id
JOIN clinic_day cd ON cd.clinic_day_id = w.clinic_day_id
WHERE w.status = 'WAITING';

-- ---------- Triggers ----------
DELIMITER $$

-- Take stock atomically; refuse the prescription if there isn't enough
CREATE TRIGGER trg_presc_before_insert
BEFORE INSERT ON prescription
FOR EACH ROW
BEGIN
    UPDATE medicine
       SET stock_qty = stock_qty - NEW.quantity
     WHERE medicine_id = NEW.medicine_id
       AND stock_qty >= NEW.quantity;
    IF ROW_COUNT() = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Medicine not found or not enough stock';
    END IF;
END$$

CREATE TRIGGER trg_presc_after_insert
AFTER INSERT ON prescription
FOR EACH ROW
BEGIN
    INSERT INTO stock_movement(medicine_id, change_qty, reason, prescription_id)
    VALUES (NEW.medicine_id, -NEW.quantity, 'PRESCRIPTION', NEW.prescription_id);
END$$

-- ---------- Procedures: medical records ----------

-- Doctor records the visit; the appointment becomes COMPLETED in the same transaction
CREATE PROCEDURE sp_record_visit(
    IN  p_appointment_id INT,
    IN  p_diagnosis      VARCHAR(255),
    IN  p_notes          TEXT,
    IN  p_followup_date  DATE,
    OUT p_visit_id       INT
)
BEGIN
    DECLARE v_status VARCHAR(20);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    IF p_diagnosis IS NULL OR TRIM(p_diagnosis) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Diagnosis is required';
    END IF;
    IF p_followup_date IS NOT NULL AND p_followup_date <= CURDATE() THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Follow-up date must be in the future';
    END IF;

    START TRANSACTION;

    SELECT status INTO v_status
      FROM appointment WHERE appointment_id = p_appointment_id FOR UPDATE;

    IF v_status IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Appointment not found';
    ELSEIF v_status <> 'BOOKED' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Only BOOKED appointments can have a visit recorded';
    END IF;

    UPDATE appointment SET status = 'COMPLETED' WHERE appointment_id = p_appointment_id;

    INSERT INTO visit(appointment_id, diagnosis, notes, followup_date)
    VALUES (p_appointment_id, p_diagnosis, p_notes, p_followup_date);
    SET p_visit_id = LAST_INSERT_ID();

    COMMIT;
END$$

-- ---------- Procedures: inventory ----------

CREATE PROCEDURE sp_add_prescription(
    IN  p_visit_id    INT,
    IN  p_medicine_id INT,
    IN  p_quantity    INT,
    IN  p_instructions VARCHAR(255),
    OUT p_prescription_id INT
)
BEGIN
    IF NOT EXISTS (SELECT 1 FROM visit WHERE visit_id = p_visit_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Visit not found';
    END IF;
    -- the BEFORE INSERT trigger checks and reduces stock
    INSERT INTO prescription(visit_id, medicine_id, quantity, instructions)
    VALUES (p_visit_id, p_medicine_id, p_quantity, p_instructions);
    SET p_prescription_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE sp_restock_medicine(
    IN p_medicine_id INT,
    IN p_quantity    INT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    IF p_quantity <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Restock quantity must be positive';
    END IF;

    START TRANSACTION;
    UPDATE medicine SET stock_qty = stock_qty + p_quantity WHERE medicine_id = p_medicine_id;
    IF ROW_COUNT() = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Medicine not found';
    END IF;
    INSERT INTO stock_movement(medicine_id, change_qty, reason)
    VALUES (p_medicine_id, p_quantity, 'RESTOCK');
    COMMIT;
END$$

-- ---------- Procedures: waitlist ----------

CREATE PROCEDURE sp_join_waitlist(
    IN  p_patient_id    INT,
    IN  p_doctor_id     INT,
    IN  p_clinic_day_id INT,
    OUT p_waitlist_id   INT
)
BEGIN
    DECLARE v_date  DATE;
    DECLARE v_open  BOOLEAN;
    DECLARE v_total INT;
    DECLARE v_free  INT;

    DECLARE EXIT HANDLER FOR 1062
    BEGIN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'You are already on this waitlist';
    END;

    SELECT clinic_date, is_open INTO v_date, v_open
      FROM clinic_day WHERE clinic_day_id = p_clinic_day_id;

    IF v_date IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Clinic day does not exist';
    ELSEIF v_open = FALSE THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Clinic is closed on that day';
    ELSEIF v_date < CURDATE() THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cannot join a waitlist for a past day';
    END IF;

    SELECT COUNT(*), IFNULL(SUM(status = 'AVAILABLE'), 0) INTO v_total, v_free
      FROM slot WHERE doctor_id = p_doctor_id AND clinic_day_id = p_clinic_day_id;

    IF v_total = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Doctor has no slots on that day';
    ELSEIF v_free > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Slots are still available, book one directly';
    END IF;

    INSERT INTO waitlist(patient_id, doctor_id, clinic_day_id)
    VALUES (p_patient_id, p_doctor_id, p_clinic_day_id);
    SET p_waitlist_id = LAST_INSERT_ID();
END$$

CREATE PROCEDURE sp_leave_waitlist(
    IN p_waitlist_id INT,
    IN p_patient_id  INT      -- pass NULL when staff removes someone
)
BEGIN
    UPDATE waitlist SET status = 'REMOVED'
     WHERE waitlist_id = p_waitlist_id
       AND status = 'WAITING'
       AND (p_patient_id IS NULL OR patient_id = p_patient_id);
    IF ROW_COUNT() = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Waitlist entry not found or already closed';
    END IF;
END$$

-- Replaces the version from 02_logic.sql: after freeing the slot,
-- the first person waiting for that doctor and day is booked into it.
DROP PROCEDURE IF EXISTS sp_cancel_appointment$$
CREATE PROCEDURE sp_cancel_appointment(
    IN p_appointment_id INT,
    IN p_patient_id     INT
)
BEGIN
    DECLARE v_status VARCHAR(20);
    DECLARE v_owner  INT;
    DECLARE v_slot   INT;
    DECLARE v_doctor INT;
    DECLARE v_day    INT;
    DECLARE v_when   DATETIME;
    DECLARE v_wl     INT DEFAULT NULL;
    DECLARE v_wp     INT DEFAULT NULL;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    SELECT a.status, a.patient_id, a.slot_id, s.doctor_id, s.clinic_day_id,
           TIMESTAMP(cd.clinic_date, s.start_time)
      INTO v_status, v_owner, v_slot, v_doctor, v_day, v_when
      FROM appointment a
      JOIN slot s        ON s.slot_id = a.slot_id
      JOIN clinic_day cd ON cd.clinic_day_id = s.clinic_day_id
     WHERE a.appointment_id = p_appointment_id
       FOR UPDATE;

    IF v_status IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Appointment not found';
    ELSEIF p_patient_id IS NOT NULL AND v_owner <> p_patient_id THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'This appointment belongs to another patient';
    ELSEIF v_status <> 'BOOKED' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Only BOOKED appointments can be cancelled';
    END IF;

    UPDATE appointment SET status = 'CANCELLED' WHERE appointment_id = p_appointment_id;

    IF v_when > NOW() THEN
        SELECT waitlist_id, patient_id INTO v_wl, v_wp
          FROM waitlist
         WHERE doctor_id = v_doctor AND clinic_day_id = v_day AND status = 'WAITING'
         ORDER BY joined_at, waitlist_id
         LIMIT 1
           FOR UPDATE;

        IF v_wl IS NOT NULL THEN
            INSERT INTO appointment(patient_id, slot_id, reason)
            VALUES (v_wp, v_slot, 'Booked from waitlist');
            UPDATE waitlist SET status = 'BOOKED' WHERE waitlist_id = v_wl;
        END IF;
    END IF;

    COMMIT;
END$$

DELIMITER ;

-- ---------- Sample medicines ----------
INSERT INTO medicine(name, unit, stock_qty, reorder_level) VALUES
 ('Paracetamol 500mg', 'tablet',  200, 50),
 ('Amoxicillin 250mg', 'capsule', 100, 30),
 ('ORS sachet',        'sachet',   80, 20),
 ('Cetirizine 10mg',   'tablet',   15, 20);
