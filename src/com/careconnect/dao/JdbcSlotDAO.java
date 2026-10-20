package com.careconnect.dao;

import com.careconnect.model.Slot;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of SlotDAO.
 * Queries v_available_slots view and calls sp_generate_slots, sp_block_slot.
 */
public class JdbcSlotDAO implements SlotDAO {

    @Override
    public List<Slot> getAvailableSlots() throws Exception {
        List<Slot> list = new ArrayList<>();
        String sql = "SELECT slot_id, clinic_date, start_time, end_time, doctor_id, doctor_name, specialization " +
                     "FROM v_available_slots ORDER BY clinic_date, start_time";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapAvailableRow(rs));
            }
        }
        return list;
    }

    @Override
    public List<Slot> getAvailableSlots(Integer doctorId, LocalDate date) throws Exception {
        List<Slot> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT slot_id, clinic_date, start_time, end_time, doctor_id, doctor_name, specialization ")
                .append("FROM v_available_slots WHERE 1=1 ");
        if (doctorId != null) {
            sql.append("AND doctor_id = ? ");
        }
        if (date != null) {
            sql.append("AND clinic_date = ? ");
        }
        sql.append("ORDER BY clinic_date, start_time");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (doctorId != null) ps.setInt(idx++, doctorId);
            if (date != null) ps.setDate(idx++, Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAvailableRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<Slot> getSlotsByDoctorAndDay(int doctorId, int clinicDayId) throws Exception {
        List<Slot> list = new ArrayList<>();
        String sql = "SELECT s.slot_id, s.doctor_id, s.clinic_day_id, s.start_time, s.end_time, s.status, " +
                     "d.full_name AS doctor_name, sp.name AS spec_name, cd.clinic_date " +
                     "FROM slot s " +
                     "JOIN doctor d ON d.doctor_id = s.doctor_id " +
                     "JOIN specialization sp ON sp.specialization_id = d.specialization_id " +
                     "JOIN clinic_day cd ON cd.clinic_day_id = s.clinic_day_id " +
                     "WHERE s.doctor_id = ? AND s.clinic_day_id = ? " +
                     "ORDER BY s.start_time";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.setInt(2, clinicDayId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Slot slot = new Slot();
                    slot.setSlotId(rs.getInt("slot_id"));
                    slot.setDoctorId(rs.getInt("doctor_id"));
                    slot.setDoctorName(rs.getString("doctor_name"));
                    slot.setSpecialization(rs.getString("spec_name"));
                    slot.setClinicDayId(rs.getInt("clinic_day_id"));
                    Date d = rs.getDate("clinic_date");
                    if (d != null) slot.setClinicDate(d.toLocalDate());
                    Time st = rs.getTime("start_time");
                    if (st != null) slot.setStartTime(st.toLocalTime());
                    Time et = rs.getTime("end_time");
                    if (et != null) slot.setEndTime(et.toLocalTime());
                    slot.setStatus(rs.getString("status"));
                    list.add(slot);
                }
            }
        }
        return list;
    }

    @Override
    public Slot getSlotById(int slotId) throws Exception {
        String sql = "SELECT s.slot_id, s.doctor_id, s.clinic_day_id, s.start_time, s.end_time, s.status, " +
                     "d.full_name AS doctor_name, sp.name AS spec_name, cd.clinic_date " +
                     "FROM slot s " +
                     "JOIN doctor d ON d.doctor_id = s.doctor_id " +
                     "JOIN specialization sp ON sp.specialization_id = d.specialization_id " +
                     "JOIN clinic_day cd ON cd.clinic_day_id = s.clinic_day_id " +
                     "WHERE s.slot_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Slot slot = new Slot();
                    slot.setSlotId(rs.getInt("slot_id"));
                    slot.setDoctorId(rs.getInt("doctor_id"));
                    slot.setDoctorName(rs.getString("doctor_name"));
                    slot.setSpecialization(rs.getString("spec_name"));
                    slot.setClinicDayId(rs.getInt("clinic_day_id"));
                    Date d = rs.getDate("clinic_date");
                    if (d != null) slot.setClinicDate(d.toLocalDate());
                    Time st = rs.getTime("start_time");
                    if (st != null) slot.setStartTime(st.toLocalTime());
                    Time et = rs.getTime("end_time");
                    if (et != null) slot.setEndTime(et.toLocalTime());
                    slot.setStatus(rs.getString("status"));
                    return slot;
                }
            }
        }
        return null;
    }

    @Override
    public void generateSlots(int doctorId, int clinicDayId, LocalTime start, LocalTime end, int slotMinutes) throws Exception {
        String call = "{CALL sp_generate_slots(?, ?, ?, ?, ?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(call)) {
            cs.setInt(1, doctorId);
            cs.setInt(2, clinicDayId);
            cs.setTime(3, Time.valueOf(start));
            cs.setTime(4, Time.valueOf(end));
            cs.setInt(5, slotMinutes);
            cs.execute();
        }
    }

    @Override
    public void blockSlot(int slotId, boolean block) throws Exception {
        String call = "{CALL sp_block_slot(?, ?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(call)) {
            cs.setInt(1, slotId);
            cs.setBoolean(2, block);
            cs.execute();
        }
    }

    private Slot mapAvailableRow(ResultSet rs) throws SQLException {
        Slot s = new Slot();
        s.setSlotId(rs.getInt("slot_id"));
        Date d = rs.getDate("clinic_date");
        if (d != null) s.setClinicDate(d.toLocalDate());
        Time st = rs.getTime("start_time");
        if (st != null) s.setStartTime(st.toLocalTime());
        Time et = rs.getTime("end_time");
        if (et != null) s.setEndTime(et.toLocalTime());
        s.setDoctorId(rs.getInt("doctor_id"));
        s.setDoctorName(rs.getString("doctor_name"));
        s.setSpecialization(rs.getString("specialization"));
        s.setStatus("AVAILABLE");
        return s;
    }
}
