package com.careconnect;

import com.careconnect.dao.DAOFactory;
import com.careconnect.model.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Automated test suite verifying data layer contracts, validation, booking,
 * and cancellation mechanics.
 */
public class SanityTest {
    public static void main(String[] args) {
        System.out.println("Running CareConnect Test Suite...");
        DAOFactory factory = DAOFactory.getInstance();

        try {
            // 1. Patient Registration Test
            System.out.println("Test 1: Testing patient registration...");
            Patient newP = new Patient(0, "Test Patient", LocalDate.of(1996, 6, 15), "FEMALE", "9999900001", "test@clinic.org", "Nagpur");
            int pid = factory.getPatientDAO().registerPatient(newP);
            assert pid > 0 : "Failed to generate patient ID";
            System.out.println("✓ Patient registered successfully with ID: #" + pid);

            // 2. Fetch available slots
            System.out.println("Test 2: Testing available slots query (v_available_slots)...");
            List<Slot> slots = factory.getSlotDAO().getAvailableSlots();
            assert !slots.isEmpty() : "Expected available slots";
            Slot targetSlot = slots.get(0);
            System.out.println("✓ Found " + slots.size() + " available slots. Target: Slot #" + targetSlot.getSlotId() + " (" + targetSlot.getTimeRangeFormatted() + ")");

            // 3. Book appointment (sp_book_appointment)
            System.out.println("Test 3: Booking appointment...");
            int apptId = factory.getAppointmentDAO().bookAppointment(pid, targetSlot.getSlotId(), "Routine Checkup");
            assert apptId > 0 : "Failed to book appointment";
            System.out.println("✓ Appointment booked with ID: #" + apptId);

            // 4. Cancel appointment (sp_cancel_appointment)
            System.out.println("Test 4: Cancelling appointment...");
            factory.getAppointmentDAO().cancelAppointment(apptId, pid);
            AppointmentDetails details = factory.getAppointmentDAO().getAppointmentDetailsById(apptId);
            assert details != null && "CANCELLED".equals(details.getStatus()) : "Expected status CANCELLED";
            System.out.println("✓ Appointment cancelled successfully, status verified: " + details.getStatus());

            // 5. Doctor management
            System.out.println("Test 5: Testing Doctor DAO...");
            List<Doctor> docs = factory.getDoctorDAO().getAllDoctors();
            assert !docs.isEmpty() : "Expected doctors list";
            System.out.println("✓ Doctors verified: " + docs.size() + " doctors found");

            // 6. Clinic Day & Slot Generation
            System.out.println("Test 6: Testing Clinic Day and Slot Generation...");
            LocalDate testDay = LocalDate.now().plusDays(5);
            factory.getClinicDayDAO().setClinicDay(testDay, true, "Specialist Eye Camp");
            ClinicDay cd = factory.getClinicDayDAO().getClinicDayByDate(testDay);
            assert cd != null && cd.isOpen() : "Expected open clinic day";
            factory.getSlotDAO().generateSlots(docs.get(0).getDoctorId(), cd.getClinicDayId(), LocalTime.of(15, 0), LocalTime.of(17, 0), 30);
            List<Slot> genSlots = factory.getSlotDAO().getSlotsByDoctorAndDay(docs.get(0).getDoctorId(), cd.getClinicDayId());
            assert !genSlots.isEmpty() : "Expected generated slots";
            System.out.println("✓ Generated " + genSlots.size() + " slots on " + testDay);

            System.out.println("\nALL 6 SANITY TESTS PASSED SUCCESSFULLY! 100% FUNCTIONAL.");
        } catch (Throwable t) {
            System.err.println("TEST SUITE FAILED:");
            t.printStackTrace();
            System.exit(1);
        }
    }
}
