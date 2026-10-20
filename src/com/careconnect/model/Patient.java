package com.careconnect.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model class representing a registered Patient.
 */
public class Patient {
    private int patientId;
    private String fullName;
    private LocalDate dob;
    private String gender; // 'FEMALE', 'MALE', 'OTHER'
    private String phone;
    private String email;
    private String address;
    private LocalDateTime createdAt;

    public Patient() {}

    public Patient(int patientId, String fullName, LocalDate dob, String gender, String phone, String email, String address) {
        this.patientId = patientId;
        this.fullName = fullName;
        this.dob = dob;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return fullName + " (ID: #" + patientId + ", Ph: " + phone + ")";
    }
}
