package com.careconnect.dao;

import com.careconnect.model.WaitlistEntry;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of WaitlistDAO.
 * Interacts with waitlist table, v_waitlist_queue view, and sp_join_waitlist / sp_leave_waitlist.
 */
public class JdbcWaitlistDAO implements WaitlistDAO {

    @Override
    public int joinWaitlist(int patientId, int doctorId, int clinicDayId) throws Exception {
        String call = "{CALL sp_join_waitlist(?, ?, ?, ?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(call)) {
            cs.setInt(1, patientId);
            cs.setInt(2, doctorId);
            cs.setInt(3, clinicDayId);
            cs.registerOutParameter(4, Types.INTEGER);

            cs.execute();
            return cs.getInt(4);
        }
    }

    @Override
    public void leaveWaitlist(int waitlistId, Integer patientId) throws Exception {
        String call = "{CALL sp_leave_waitlist(?, ?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(call)) {
            cs.setInt(1, waitlistId);
            if (patientId != null) {
                cs.setInt(2, patientId);
            } else {
                cs.setNull(2, Types.INTEGER);
            }
            cs.execute();
        }
    }

    @Override
    public List<WaitlistEntry> getWaitlistQueue(Integer doctorId, Integer clinicDayId) throws Exception {
        List<WaitlistEntry> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT w.waitlist_id, p.patient_id, p.full_name AS patient_name, ")
                .append("d.doctor_id, d.full_name AS doctor_name, cd.clinic_day_id, cd.clinic_date, ")
                .append("w.status, ")
                .append("ROW_NUMBER() OVER (PARTITION BY w.doctor_id, w.clinic_day_id ORDER BY w.joined_at, w.waitlist_id) AS position_in_queue ")
                .append("FROM waitlist w ")
                .append("JOIN patient p ON p.patient_id = w.patient_id ")
                .append("JOIN doctor d ON d.doctor_id = w.doctor_id ")
                .append("JOIN clinic_day cd ON cd.clinic_day_id = w.clinic_day_id ")
                .append("WHERE w.status = 'WAITING' ");

        if (doctorId != null && doctorId > 0) {
            sql.append("AND w.doctor_id = ? ");
        }
        if (clinicDayId != null && clinicDayId > 0) {
            sql.append("AND w.clinic_day_id = ? ");
        }
        sql.append("ORDER BY cd.clinic_date, position_in_queue");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (doctorId != null && doctorId > 0) {
                ps.setInt(idx++, doctorId);
            }
            if (clinicDayId != null && clinicDayId > 0) {
                ps.setInt(idx++, clinicDayId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    WaitlistEntry we = new WaitlistEntry();
                    we.setWaitlistId(rs.getInt("waitlist_id"));
                    we.setPatientId(rs.getInt("patient_id"));
                    we.setPatientName(rs.getString("patient_name"));
                    we.setDoctorId(rs.getInt("doctor_id"));
                    we.setDoctorName(rs.getString("doctor_name"));
                    we.setClinicDayId(rs.getInt("clinic_day_id"));
                    Date d = rs.getDate("clinic_date");
                    if (d != null) we.setClinicDate(d.toLocalDate());
                    we.setStatus(rs.getString("status"));
                    we.setPositionInQueue(rs.getInt("position_in_queue"));
                    list.add(we);
                }
            }
        }
        return list;
    }
}
