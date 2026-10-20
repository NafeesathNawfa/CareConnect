package com.careconnect.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Model class representing an appointment slot.
 */
public class Slot {
    private int slotId;
    private int doctorId;
    private String doctorName;
    private String specialization;
    private int clinicDayId;
    private LocalDate clinicDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status; // 'AVAILABLE', 'BOOKED', 'BLOCKED'

    public Slot() {}

    public Slot(int slotId, int doctorId, int clinicDayId, LocalTime startTime, LocalTime endTime, String status) {
        this.slotId = slotId;
        this.doctorId = doctorId;
        this.clinicDayId = clinicDayId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public Slot(int slotId, int doctorId, String doctorName, String specialization, LocalDate clinicDate, LocalTime startTime, LocalTime endTime, String status) {
        this.slotId = slotId;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.specialization = specialization;
        this.clinicDate = clinicDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimeRangeFormatted() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("hh:mm a");
        if (startTime != null && endTime != null) {
            return startTime.format(fmt) + " - " + endTime.format(fmt);
        }
        return "";
    }

    @Override
    public String toString() {
        return getTimeRangeFormatted() + " [" + status + "]";
    }
}
