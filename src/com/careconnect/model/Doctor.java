package com.careconnect.model;

/**
 * Model class representing a Doctor or Volunteer Physician.
 */
public class Doctor {
    private int doctorId;
    private String fullName;
    private int specializationId;
    private String specializationName;
    private String phone;
    private String email;
    private boolean isVolunteer;
    private boolean isActive;

    public Doctor() {}

    public Doctor(int doctorId, String fullName, int specializationId, String phone, String email, boolean isVolunteer, boolean isActive) {
        this.doctorId = doctorId;
        this.fullName = fullName;
        this.specializationId = specializationId;
        this.phone = phone;
        this.email = email;
        this.isVolunteer = isVolunteer;
        this.isActive = isActive;
    }

    public Doctor(int doctorId, String fullName, int specializationId, String specializationName, String phone, String email, boolean isVolunteer, boolean isActive) {
        this(doctorId, fullName, specializationId, phone, email, isVolunteer, isActive);
        this.specializationName = specializationName;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getSpecializationId() {
        return specializationId;
    }

    public void setSpecializationId(int specializationId) {
        this.specializationId = specializationId;
    }

    public String getSpecializationName() {
        return specializationName;
    }

    public void setSpecializationName(String specializationName) {
        this.specializationName = specializationName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isVolunteer() {
        return isVolunteer;
    }

    public void setVolunteer(boolean volunteer) {
        isVolunteer = volunteer;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return fullName + (specializationName != null ? " (" + specializationName + ")" : "");
    }
}
