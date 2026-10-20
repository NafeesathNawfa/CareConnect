package com.careconnect.dao;

import com.careconnect.model.AppointmentDetails;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of AppointmentDAO.
 * Interacts with appointment table, v_appointment_details, v_todays_schedule,
 * and calls sp_book_appointment, sp_cancel_appointment, and sp_set_appointment_outcome.
 */
public class JdbcAppointmentDAO implements AppointmentDAO {

    @Override
    public int bookAppointment(int patientId, int slotId, String reason) throws Exception {
        String call = "{CALL sp_book_appointment(?, ?, ?, ?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(call)) {
            cs.setInt(1, patientId);
            cs.setInt(2, slotId);
            cs.setString(3, reason);
            cs.registerOutParameter(4, Types.INTEGER);

            cs.execute();
            return cs.getInt(4);
        }
    }

    @Override
    public void cancelAppointment(int appointmentId, Integer patientId) throws Exception {
        String call = "{CALL sp_cancel_appointment(?, ?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(call)) {
            cs.setInt(1, appointmentId);
            if (patientId != null) {
                cs.setInt(2, patientId);
            } else {
                cs.setNull(2, Types.INTEGER);
            }
            cs.execute();
        }
    }

    @Override
    public void setAppointmentOutcome(int appointmentId, String outcome) throws Exception {
        String call = "{CALL sp_set_appointment_outcome(?, ?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(call)) {
            cs.setInt(1, appointmentId);
            cs.setString(2, outcome);
            cs.execute();
        }
    }

    @Override
    public List<AppointmentDetails> getAppointmentsByPatient(int patientId) throws Exception {
        List<AppointmentDetails> list = new ArrayList<>();
        String sql = "SELECT appointment_id, status, reason, patient_id, patient_name, patient_phone, " +
                     "doctor_id, doctor_name, clinic_date, start_time, end_time " +
                     "FROM v_appointment_details WHERE patient_id = ? " +
                     "ORDER BY clinic_date DESC, start_time DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapDetails(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<AppointmentDetails> getTodaysSchedule() throws Exception {
        List<AppointmentDetails> list = new ArrayList<>();
        String sql = "SELECT appointment_id, status, reason, patient_id, patient_name, patient_phone, " +
                     "doctor_id, doctor_name, clinic_date, start_time, end_time " +
                     "FROM v_todays_schedule ORDER BY start_time";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapDetails(rs));
            }
        }
        return list;
    }

    @Override
    public List<AppointmentDetails> getScheduleByDoctor(int doctorId, LocalDate date) throws Exception {
        List<AppointmentDetails> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT appointment_id, status, reason, patient_id, patient_name, patient_phone, ")
                .append("doctor_id, doctor_name, clinic_date, start_time, end_time ")
                .append("FROM v_appointment_details WHERE doctor_id = ? ");
        if (date != null) {
            sql.append("AND clinic_date = ? ");
        }
        sql.append("ORDER BY clinic_date, start_time");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, doctorId);
            if (date != null) {
                ps.setDate(2, Date.valueOf(date));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapDetails(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<AppointmentDetails> getAllAppointments(String statusFilter, Integer doctorId, String search) throws Exception {
        List<AppointmentDetails> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT appointment_id, status, reason, patient_id, patient_name, patient_phone, ")
                .append("doctor_id, doctor_name, clinic_date, start_time, end_time ")
                .append("FROM v_appointment_details WHERE 1=1 ");

        if (statusFilter != null && !statusFilter.equalsIgnoreCase("ALL") && !statusFilter.trim().isEmpty()) {
            sql.append("AND status = ? ");
        }
        if (doctorId != null && doctorId > 0) {
            sql.append("AND doctor_id = ? ");
        }
        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (LOWER(patient_name) LIKE ? OR patient_phone LIKE ? OR LOWER(doctor_name) LIKE ?) ");
        }
        sql.append("ORDER BY clinic_date DESC, start_time DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (statusFilter != null && !statusFilter.equalsIgnoreCase("ALL") && !statusFilter.trim().isEmpty()) {
                ps.setString(idx++, statusFilter.toUpperCase());
            }
            if (doctorId != null && doctorId > 0) {
                ps.setInt(idx++, doctorId);
            }
            if (search != null && !search.trim().isEmpty()) {
                String term = "%" + search.trim().toLowerCase() + "%";
                ps.setString(idx++, term);
                ps.setString(idx++, term);
                ps.setString(idx++, term);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapDetails(rs));
                }
            }
        }
        return list;
    }

    @Override
    public AppointmentDetails getAppointmentDetailsById(int appointmentId) throws Exception {
        String sql = "SELECT appointment_id, status, reason, patient_id, patient_name, patient_phone, " +
                     "doctor_id, doctor_name, clinic_date, start_time, end_time " +
                     "FROM v_appointment_details WHERE appointment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapDetails(rs);
                }
            }
        }
        return null;
    }

    private AppointmentDetails mapDetails(ResultSet rs) throws SQLException {
        AppointmentDetails ad = new AppointmentDetails();
        ad.setAppointmentId(rs.getInt("appointment_id"));
        ad.setStatus(rs.getString("status"));
        ad.setReason(rs.getString("reason"));
        ad.setPatientId(rs.getInt("patient_id"));
        ad.setPatientName(rs.getString("patient_name"));
        ad.setPatientPhone(rs.getString("patient_phone"));
        ad.setDoctorId(rs.getInt("doctor_id"));
        ad.setDoctorName(rs.getString("doctor_name"));
        Date d = rs.getDate("clinic_date");
        if (d != null) ad.setClinicDate(d.toLocalDate());
        Time st = rs.getTime("start_time");
        if (st != null) ad.setStartTime(st.toLocalTime());
        Time et = rs.getTime("end_time");
        if (et != null) ad.setEndTime(et.toLocalTime());
        return ad;
    }
}
