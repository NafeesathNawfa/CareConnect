-- =====================================================
-- Demo for the extensions. Run on a FRESH database:
--   01_schema -> 02_logic -> 03_seed -> 05_extensions -> this file
-- (Don't run 04 first; both use the same sample slots.)
-- =====================================================
USE free_clinic;

-- A fourth patient, registered through the procedure
CALL sp_register_patient('Kiran Das', '1985-03-14', 'OTHER', '8000000004', NULL, 'Nagpur', @p4);

-- ---- Waitlist: Dr. Imran (doctor 2) has slots 7-10 on clinic day 1 ----
CALL sp_book_appointment(1, 7,  'Fever',      @a7);
CALL sp_book_appointment(2, 8,  'Cough',      @a8);
CALL sp_book_appointment(3, 9,  'Vaccination',@a9);
CALL sp_book_appointment(1, 10, 'Follow-up',  @a10);

-- Day is now full, so Kiran and then Sana join the waitlist
CALL sp_join_waitlist(@p4, 2, 1, @w1);
CALL sp_join_waitlist(2,   2, 1, @w2);
SELECT * FROM v_waitlist_queue;

-- Joining twice, or while slots are free, is refused:
-- CALL sp_join_waitlist(@p4, 2, 1, @x);   -- already on this waitlist
-- CALL sp_join_waitlist(@p4, 1, 1, @x);   -- slots still available

-- Ravi cancels slot 7 -> Kiran (first in queue) is booked automatically
CALL sp_cancel_appointment(@a7, 1);
SELECT appointment_id, patient_name, start_time, status, reason
FROM v_appointment_details WHERE doctor_id = 2 ORDER BY start_time;
SELECT * FROM v_waitlist_queue;

-- ---- Medical record + prescription ----
CALL sp_record_visit(@a10, 'Viral fever', 'Rest and plenty of fluids', CURDATE() + INTERVAL 7 DAY, @v1);
CALL sp_add_prescription(@v1, 1, 10, '1 tablet, 3 times a day', @pr1);
CALL sp_add_prescription(@v1, 3, 5,  '1 sachet after each loose stool', @pr2);

-- Too many tablets is refused by the stock trigger:
-- CALL sp_add_prescription(@v1, 4, 500, 'Too many', @x);   -- not enough stock

SELECT * FROM v_patient_history;

-- ---- Inventory ----
SELECT * FROM medicine;
SELECT * FROM v_low_stock;                 -- Cetirizine is below its reorder level
CALL sp_restock_medicine(4, 100);
SELECT * FROM v_low_stock;                 -- now empty
SELECT * FROM stock_movement;
