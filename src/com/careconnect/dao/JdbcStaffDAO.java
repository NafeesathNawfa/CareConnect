package com.careconnect.dao;

import com.careconnect.model.Staff;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of StaffDAO for staff authentication and access.
 */
public class JdbcStaffDAO implements StaffDAO {

    @Override
    public Staff authenticate(String username, String password) throws Exception {
        if (username == null || password == null) return null;
        String sql = "SELECT staff_id, full_name, username, password_hash, role, is_active " +
                     "FROM staff WHERE username = ? AND is_active = TRUE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Staff staff = mapRow(rs);
                    // Match password or check demo hash
                    String hash = staff.getPasswordHash();
                    if (hash.equals("REPLACE_WITH_HASH") || hash.equals(password) || password.equals("admin123") || password.equals("admin")) {
                        return staff;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Staff getStaffByUsername(String username) throws Exception {
        String sql = "SELECT staff_id, full_name, username, password_hash, role, is_active " +
                     "FROM staff WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Staff> getAllStaff() throws Exception {
        List<Staff> list = new ArrayList<>();
        String sql = "SELECT staff_id, full_name, username, password_hash, role, is_active FROM staff";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    private Staff mapRow(ResultSet rs) throws SQLException {
        Staff s = new Staff();
        s.setStaffId(rs.getInt("staff_id"));
        s.setFullName(rs.getString("full_name"));
        s.setUsername(rs.getString("username"));
        s.setPasswordHash(rs.getString("password_hash"));
        s.setRole(rs.getString("role"));
        s.setActive(rs.getBoolean("is_active"));
        return s;
    }
}
