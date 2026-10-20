package com.careconnect.dao;

import com.careconnect.model.AppointmentDetails;
import java.time.LocalDate;
import java.util.List;

/**
 * Data Access Object interface for appointments.
 * Backed by appointment table, v_appointment_details, v_todays_schedule,
 * and sp_book_appointment, sp_cancel_appointment, sp_set_appointment_outcome.
 */
public interface AppointmentDAO {
    int bookAppointment(int patientId, int slotId, String reason) throws Exception;
    void cancelAppointment(int appointmentId, Integer patientId) throws Exception;
    void setAppointmentOutcome(int appointmentId, String outcome) throws Exception;
    List<AppointmentDetails> getAppointmentsByPatient(int patientId) throws Exception;
    List<AppointmentDetails> getTodaysSchedule() throws Exception;
    List<AppointmentDetails> getScheduleByDoctor(int doctorId, LocalDate date) throws Exception;
    List<AppointmentDetails> getAllAppointments(String statusFilter, Integer doctorId, String search) throws Exception;
    AppointmentDetails getAppointmentDetailsById(int appointmentId) throws Exception;
}
