package com.careconnect.model;

import java.time.LocalDateTime;

/**
 * Model class representing an Appointment entity.
 */
public class Appointment {
    private int appointmentId;
    private int patientId;
    private int slotId;
    private String status; // 'BOOKED', 'COMPLETED', 'CANCELLED', 'NO_SHOW'
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Appointment() {}

    public Appointment(int appointmentId, int patientId, int slotId, String status, String reason) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.slotId = slotId;
        this.status = status;
        this.reason = reason;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isEligibleForCancellation() {
        return "BOOKED".equalsIgnoreCase(status);
    }
}
