package com.careconnect.dao;

import com.careconnect.model.ClinicDay;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of ClinicDayDAO.
 */
public class JdbcClinicDayDAO implements ClinicDayDAO {

    @Override
    public List<ClinicDay> getAllClinicDays() throws Exception {
        List<ClinicDay> list = new ArrayList<>();
        String sql = "SELECT clinic_day_id, clinic_date, is_open, notes FROM clinic_day ORDER BY clinic_date";
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
    public List<ClinicDay> getUpcomingClinicDays() throws Exception {
        List<ClinicDay> list = new ArrayList<>();
        String sql = "SELECT clinic_day_id, clinic_date, is_open, notes FROM clinic_day " +
                     "WHERE clinic_date >= CURDATE() ORDER BY clinic_date";
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
    public ClinicDay getClinicDayById(int clinicDayId) throws Exception {
        String sql = "SELECT clinic_day_id, clinic_date, is_open, notes FROM clinic_day WHERE clinic_day_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clinicDayId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public ClinicDay getClinicDayByDate(LocalDate date) throws Exception {
        String sql = "SELECT clinic_day_id, clinic_date, is_open, notes FROM clinic_day WHERE clinic_date = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public void setClinicDay(LocalDate date, boolean isOpen, String notes) throws Exception {
        String call = "{CALL sp_set_clinic_day(?, ?, ?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(call)) {
            cs.setDate(1, Date.valueOf(date));
            cs.setBoolean(2, isOpen);
            cs.setString(3, notes);
            cs.execute();
        }
    }

    private ClinicDay mapRow(ResultSet rs) throws SQLException {
        ClinicDay cd = new ClinicDay();
        cd.setClinicDayId(rs.getInt("clinic_day_id"));
        Date d = rs.getDate("clinic_date");
        if (d != null) cd.setClinicDate(d.toLocalDate());
        cd.setOpen(rs.getBoolean("is_open"));
        cd.setNotes(rs.getString("notes"));
        return cd;
    }
}
