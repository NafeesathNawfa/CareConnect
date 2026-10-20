package com.careconnect.dao;

import com.careconnect.model.WaitlistEntry;
import java.util.List;

/**
 * Data Access Object interface for Waitlist queue operations.
 * Backed by waitlist table, v_waitlist_queue, and sp_join_waitlist / sp_leave_waitlist.
 */
public interface WaitlistDAO {
    int joinWaitlist(int patientId, int doctorId, int clinicDayId) throws Exception;
    void leaveWaitlist(int waitlistId, Integer patientId) throws Exception;
    List<WaitlistEntry> getWaitlistQueue(Integer doctorId, Integer clinicDayId) throws Exception;
}
