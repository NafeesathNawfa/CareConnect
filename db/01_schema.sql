-- =====================================================
-- Free Clinic Appointment System : schema (MySQL 8+)
-- Run order: 01_schema -> 02_logic -> 03_seed
-- =====================================================
DROP DATABASE IF EXISTS free_clinic;
CREATE DATABASE free_clinic CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE free_clinic;

-- ---------- Lookup ----------
CREATE TABLE specialization (
    specialization_id INT AUTO_INCREMENT PRIMARY KEY,
    name              VARCHAR(60) NOT NULL UNIQUE
) ENGINE=InnoDB;

-- ---------- People ----------
CREATE TABLE doctor (
    doctor_id         INT AUTO_INCREMENT PRIMARY KEY,
    full_name         VARCHAR(100) NOT NULL,
    specialization_id INT NOT NULL,
    phone             VARCHAR(15)  NOT NULL,
    email             VARCHAR(100) UNIQUE,
    is_volunteer      BOOLEAN NOT NULL DEFAULT TRUE,
    is_active         BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_doctor_spec FOREIGN KEY (specialization_id)
        REFERENCES specialization(specialization_id)
) ENGINE=InnoDB;

CREATE TABLE patient (
    patient_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name  VARCHAR(100) NOT NULL,
    dob        DATE NOT NULL,
    gender     ENUM('FEMALE','MALE','OTHER') NOT NULL,
    phone      VARCHAR(15) NOT NULL UNIQUE,
    email      VARCHAR(100),
    address    VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_patient_dob CHECK (dob <= '2100-01-01')
) ENGINE=InnoDB;

CREATE TABLE staff (
    staff_id      INT AUTO_INCREMENT PRIMARY KEY,
    full_name     VARCHAR(100) NOT NULL,
    username      VARCHAR(40)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,          -- store a hash (e.g. BCrypt) from Java, never plain text
    role          ENUM('ADMIN','RECEPTIONIST') NOT NULL,
    is_active     BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

-- ---------- Scheduling ----------
CREATE TABLE clinic_day (
    clinic_day_id INT AUTO_INCREMENT PRIMARY KEY,
    clinic_date   DATE NOT NULL UNIQUE,
    is_open       BOOLEAN NOT NULL DEFAULT TRUE,
    notes         VARCHAR(255)
) ENGINE=InnoDB;

CREATE TABLE slot (
    slot_id       INT AUTO_INCREMENT PRIMARY KEY,
    doctor_id     INT NOT NULL,
    clinic_day_id INT NOT NULL,
    start_time    TIME NOT NULL,
    end_time      TIME NOT NULL,
    status        ENUM('AVAILABLE','BOOKED','BLOCKED') NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT fk_slot_doctor FOREIGN KEY (doctor_id)     REFERENCES doctor(doctor_id),
    CONSTRAINT fk_slot_day    FOREIGN KEY (clinic_day_id) REFERENCES clinic_day(clinic_day_id),
    CONSTRAINT uq_slot UNIQUE (doctor_id, clinic_day_id, start_time),
    CONSTRAINT chk_slot_time CHECK (end_time > start_time)
) ENGINE=InnoDB;

-- ---------- Appointments ----------
CREATE TABLE appointment (
    appointment_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id     INT NOT NULL,
    slot_id        INT NOT NULL,
    status         ENUM('BOOKED','COMPLETED','CANCELLED','NO_SHOW') NOT NULL DEFAULT 'BOOKED',
    reason         VARCHAR(255),
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    -- Generated column: holds slot_id for every non-cancelled appointment, NULL otherwise.
    -- UNIQUE on it => a slot can have only ONE live appointment (blocks double booking
    -- even if two people click at the same moment). Cancelled rows become NULL, freeing the slot.
    active_slot_id INT GENERATED ALWAYS AS (IF(status = 'CANCELLED', NULL, slot_id)) VIRTUAL,
    CONSTRAINT fk_appt_patient FOREIGN KEY (patient_id) REFERENCES patient(patient_id),
    CONSTRAINT fk_appt_slot    FOREIGN KEY (slot_id)    REFERENCES slot(slot_id),
    CONSTRAINT uq_active_slot UNIQUE (active_slot_id)
) ENGINE=InnoDB;

-- ---------- Audit ----------
CREATE TABLE appointment_log (
    log_id         INT AUTO_INCREMENT PRIMARY KEY,
    appointment_id INT NOT NULL,
    old_status     VARCHAR(20),
    new_status     VARCHAR(20) NOT NULL,
    changed_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------- Indexes ----------
CREATE INDEX idx_slot_day_status  ON slot(clinic_day_id, status);
CREATE INDEX idx_appt_patient     ON appointment(patient_id, status);
CREATE INDEX idx_appt_status      ON appointment(status);
CREATE INDEX idx_day_date         ON clinic_day(clinic_date);
