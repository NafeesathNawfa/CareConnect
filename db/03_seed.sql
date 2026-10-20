-- =====================================================
-- Sample data for testing
-- =====================================================
USE free_clinic;

INSERT INTO specialization(name) VALUES
 ('General Medicine'), ('Pediatrics'), ('Dentistry'), ('Gynecology');

INSERT INTO doctor(full_name, specialization_id, phone, email, is_volunteer) VALUES
 ('Dr. Anita Rao',   1, '9000000001', 'anita@clinic.org',  TRUE),
 ('Dr. Imran Khan',  2, '9000000002', 'imran@clinic.org',  TRUE),
 ('Dr. Meera Nair',  3, '9000000003', 'meera@clinic.org',  FALSE);

-- NOTE: replace these with real BCrypt hashes generated from Java
INSERT INTO staff(full_name, username, password_hash, role) VALUES
 ('Clinic Admin', 'admin',     'REPLACE_WITH_HASH', 'ADMIN'),
 ('Front Desk',   'reception', 'REPLACE_WITH_HASH', 'RECEPTIONIST');

INSERT INTO patient(full_name, dob, gender, phone, email, address) VALUES
 ('Ravi Kumar',  '1990-05-12', 'MALE',   '8000000001', 'ravi@mail.com',  'Nagpur'),
 ('Sana Sheikh', '2001-11-03', 'FEMALE', '8000000002', 'sana@mail.com',  'Nagpur'),
 ('Aarav Patil', '2016-02-20', 'MALE',   '8000000003', NULL,             'Nagpur');

-- Clinic days: next 3 days from today
CALL sp_set_clinic_day(CURDATE() + INTERVAL 1 DAY, TRUE, 'Regular OPD');
CALL sp_set_clinic_day(CURDATE() + INTERVAL 2 DAY, TRUE, 'Regular OPD');
CALL sp_set_clinic_day(CURDATE() + INTERVAL 3 DAY, FALSE, 'Closed - holiday');

-- 20-minute slots, 9:00 to 11:00
CALL sp_generate_slots(1, 1, '09:00', '11:00', 20);
CALL sp_generate_slots(2, 1, '10:00', '12:00', 30);
CALL sp_generate_slots(3, 2, '14:00', '16:00', 30);
