package com.careconnect.dao;

import com.careconnect.model.Doctor;
import com.careconnect.model.Specialization;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of DoctorDAO.
 */
public class JdbcDoctorDAO implements DoctorDAO {

    @Override
    public List<Doctor> getAllDoctors() throws Exception {
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT d.doctor_id, d.full_name, d.specialization_id, sp.name AS spec_name, " +
                     "d.phone, d.email, d.is_volunteer, d.is_active " +
                     "FROM doctor d " +
                     "JOIN specialization sp ON sp.specialization_id = d.specialization_id " +
                     "ORDER BY d.full_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public List<Doctor> getActiveDoctors() throws Exception {
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT d.doctor_id, d.full_name, d.specialization_id, sp.name AS spec_name, " +
                     "d.phone, d.email, d.is_volunteer, d.is_active " +
                     "FROM doctor d " +
                     "JOIN specialization sp ON sp.specialization_id = d.specialization_id " +
                     "WHERE d.is_active = TRUE " +
                     "ORDER BY d.full_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public Doctor getDoctorById(int doctorId) throws Exception {
        String sql = "SELECT d.doctor_id, d.full_name, d.specialization_id, sp.name AS spec_name, " +
                     "d.phone, d.email, d.is_volunteer, d.is_active " +
                     "FROM doctor d " +
                     "JOIN specialization sp ON sp.specialization_id = d.specialization_id " +
                     "WHERE d.doctor_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public int addDoctor(Doctor doctor) throws Exception {
        String sql = "INSERT INTO doctor (full_name, specialization_id, phone, email, is_volunteer, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, doctor.getFullName());
            ps.setInt(2, doctor.getSpecializationId());
            ps.setString(3, doctor.getPhone());
            ps.setString(4, doctor.getEmail());
            ps.setBoolean(5, doctor.isVolunteer());
            ps.setBoolean(6, doctor.isActive());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    doctor.setDoctorId(id);
                    return id;
                }
            }
        }
        return 0;
    }

    @Override
    public boolean updateDoctor(Doctor doctor) throws Exception {
        String sql = "UPDATE doctor SET full_name = ?, specialization_id = ?, phone = ?, email = ?, " +
                     "is_volunteer = ?, is_active = ? WHERE doctor_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doctor.getFullName());
            ps.setInt(2, doctor.getSpecializationId());
            ps.setString(3, doctor.getPhone());
            ps.setString(4, doctor.getEmail());
            ps.setBoolean(5, doctor.isVolunteer());
            ps.setBoolean(6, doctor.isActive());
            ps.setInt(7, doctor.getDoctorId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean toggleDoctorActive(int doctorId, boolean active) throws Exception {
        String sql = "UPDATE doctor SET is_active = ? WHERE doctor_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setInt(2, doctorId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Specialization> getAllSpecializations() throws Exception {
        List<Specialization> list = new ArrayList<>();
        String sql = "SELECT specialization_id, name FROM specialization ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Specialization(rs.getInt("specialization_id"), rs.getString("name")));
            }
        }
        return list;
    }

    private Doctor mapRow(ResultSet rs) throws SQLException {
        Doctor d = new Doctor();
        d.setDoctorId(rs.getInt("doctor_id"));
        d.setFullName(rs.getString("full_name"));
        d.setSpecializationId(rs.getInt("specialization_id"));
        d.setSpecializationName(rs.getString("spec_name"));
        d.setPhone(rs.getString("phone"));
        d.setEmail(rs.getString("email"));
        d.setVolunteer(rs.getBoolean("is_volunteer"));
        d.setActive(rs.getBoolean("is_active"));
        return d;
    }
}
