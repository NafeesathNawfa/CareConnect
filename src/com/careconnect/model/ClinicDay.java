package com.careconnect.model;

import java.time.LocalDate;

/**
 * Model class representing a scheduled clinic operation day.
 */
public class ClinicDay {
    private int clinicDayId;
    private LocalDate clinicDate;
    private boolean isOpen;
    private String notes;

    public ClinicDay() {}

    public ClinicDay(int clinicDayId, LocalDate clinicDate, boolean isOpen, String notes) {
        this.clinicDayId = clinicDayId;
        this.clinicDate = clinicDate;
        this.isOpen = isOpen;
        this.notes = notes;
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

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return (clinicDate != null ? clinicDate.toString() : "") + (isOpen ? " (Open - " + (notes != null ? notes : "") + ")" : " (Closed)");
    }
}
