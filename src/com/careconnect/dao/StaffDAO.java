package com.careconnect.dao;

import com.careconnect.model.Staff;
import java.util.List;

/**
 * Data Access Object interface for Staff authentication and management.
 */
public interface StaffDAO {
    Staff authenticate(String username, String password) throws Exception;
    Staff getStaffByUsername(String username) throws Exception;
    List<Staff> getAllStaff() throws Exception;
}
