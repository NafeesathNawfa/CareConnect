package com.careconnect.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Model class representing the v_appointment_details database view.
 */
public class AppointmentDetails {
    private int appointmentId;
    private String status; // 'BOOKED', 'COMPLETED', 'CANCELLED', 'NO_SHOW'
    private String reason;
    private int patientId;
    private String patientName;
    private String patientPhone;
    private int doctorId;
    private String doctorName;
    private String specialization;
    private LocalDate clinicDate;
    private LocalTime startTime;
    private LocalTime endTime;

    public AppointmentDetails() {}

    public AppointmentDetails(int appointmentId, String status, String reason, int patientId,
                              String patientName, String patientPhone, int doctorId,
                              String doctorName, LocalDate clinicDate,
                              LocalTime startTime, LocalTime endTime) {
        this.appointmentId = appointmentId;
        this.status = status;
        this.reason = reason;
        this.patientId = patientId;
        this.patientName = patientName;
        this.patientPhone = patientPhone;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.clinicDate = clinicDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public LocalDate getClinicDate() {
        return clinicDate;
    }

    public void setClinicDate(LocalDate clinicDate) {
        this.clinicDate = clinicDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getTimeRangeFormatted() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("hh:mm a");
        if (startTime != null && endTime != null) {
            return startTime.format(fmt) + " - " + endTime.format(fmt);
        }
        return "";
    }

    public boolean isEligibleForCancellation() {
        return "BOOKED".equalsIgnoreCase(status);
    }
}
