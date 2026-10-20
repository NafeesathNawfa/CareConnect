package com.careconnect.dao;

import com.careconnect.model.Patient;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of PatientDAO invoking sp_register_patient and patient table queries.
 */
public class JdbcPatientDAO implements PatientDAO {

    @Override
    public int registerPatient(Patient patient) throws Exception {
        String call = "{CALL sp_register_patient(?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(call)) {
            cs.setString(1, patient.getFullName());
            cs.setDate(2, Date.valueOf(patient.getDob()));
            cs.setString(3, patient.getGender());
            cs.setString(4, patient.getPhone());
            cs.setString(5, patient.getEmail());
            cs.setString(6, patient.getAddress());
            cs.registerOutParameter(7, Types.INTEGER);

            cs.execute();
            int newId = cs.getInt(7);
            patient.setPatientId(newId);
            return newId;
        }
    }

    @Override
    public Patient getPatientById(int patientId) throws Exception {
        String sql = "SELECT patient_id, full_name, dob, gender, phone, email, address, created_at FROM patient WHERE patient_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Patient getPatientByPhone(String phone) throws Exception {
        String sql = "SELECT patient_id, full_name, dob, gender, phone, email, address, created_at FROM patient WHERE phone = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Patient> getAllPatients() throws Exception {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT patient_id, full_name, dob, gender, phone, email, address, created_at FROM patient ORDER BY full_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    private Patient mapRow(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setPatientId(rs.getInt("patient_id"));
        p.setFullName(rs.getString("full_name"));
        Date d = rs.getDate("dob");
        if (d != null) p.setDob(d.toLocalDate());
        p.setGender(rs.getString("gender"));
        p.setPhone(rs.getString("phone"));
        p.setEmail(rs.getString("email"));
        p.setAddress(rs.getString("address"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) p.setCreatedAt(ts.toLocalDateTime());
        return p;
    }
}
