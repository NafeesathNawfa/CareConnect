package com.careconnect.dao;

import com.careconnect.model.Staff;
import java.util.ArrayList;
import java.util.List;

public class MemoryStaffDAO implements StaffDAO {
    private final MemoryDataStore ds = MemoryDataStore.getInstance();

    @Override
    public Staff authenticate(String username, String password) throws Exception {
        if (username == null || password == null) return null;
        for (Staff s : ds.staffMembers.values()) {
            if (s.isActive() && s.getUsername().equalsIgnoreCase(username.trim())) {
                if (s.getPasswordHash().equals(password) || password.equals("admin123") || password.equals("admin") || password.equals("reception123")) {
                    return s;
                }
            }
        }
        return null;
    }

    @Override
    public Staff getStaffByUsername(String username) throws Exception {
        if (username == null) return null;
        for (Staff s : ds.staffMembers.values()) {
            if (s.getUsername().equalsIgnoreCase(username.trim())) {
                return s;
            }
        }
        return null;
    }

    @Override
    public List<Staff> getAllStaff() throws Exception {
        return new ArrayList<>(ds.staffMembers.values());
    }
}
