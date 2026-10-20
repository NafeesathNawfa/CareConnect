package com.careconnect.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model class representing a Waitlist Entry from v_waitlist_queue.
 */
public class WaitlistEntry {
    private int waitlistId;
    private int patientId;
    private String patientName;
    private int doctorId;
    private String doctorName;
    private int clinicDayId;
    private LocalDate clinicDate;
    private String status; // 'WAITING', 'BOOKED', 'REMOVED'
    private int positionInQueue;
    private LocalDateTime joinedAt;

    public WaitlistEntry() {}

    public WaitlistEntry(int waitlistId, int patientId, String patientName, int doctorId,
                         String doctorName, int clinicDayId, LocalDate clinicDate,
                         String status, int positionInQueue) {
        this.waitlistId = waitlistId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.clinicDayId = clinicDayId;
        this.clinicDate = clinicDate;
        this.status = status;
        this.positionInQueue = positionInQueue;
    }

    public int getWaitlistId() {
        return waitlistId;
    }

    public void setWaitlistId(int waitlistId) {
        this.waitlistId = waitlistId;
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

    public int getClinicDayId() {
        return clinicDayId;
    }

    public void setClinicDayId(int clinicDayId) {
        this.clinicDayId = clinicDayId;
    }

    public LocalDate getClinicDate() {
        return clinicDate;
    }

    public void setClinicDate(LocalDate clinicDate) {
        this.clinicDate = clinicDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getPositionInQueue() {
        return positionInQueue;
    }

    public void setPositionInQueue(int positionInQueue) {
        this.positionInQueue = positionInQueue;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}
