package com.careconnect.dao;

import com.careconnect.model.WaitlistEntry;
import java.util.List;
import java.util.stream.Collectors;

public class MemoryWaitlistDAO implements WaitlistDAO {
    private final MemoryDataStore ds = MemoryDataStore.getInstance();

    @Override
    public int joinWaitlist(int patientId, int doctorId, int clinicDayId) throws Exception {
        return ds.joinWaitlist(patientId, doctorId, clinicDayId);
    }

    @Override
    public void leaveWaitlist(int waitlistId, Integer patientId) throws Exception {
        ds.leaveWaitlist(waitlistId, patientId);
    }

    @Override
    public List<WaitlistEntry> getWaitlistQueue(Integer doctorId, Integer clinicDayId) throws Exception {
        return ds.waitlist.values().stream()
                .filter(w -> "WAITING".equals(w.getStatus()))
                .filter(w -> doctorId == null || doctorId == 0 || w.getDoctorId() == doctorId)
                .filter(w -> clinicDayId == null || clinicDayId == 0 || w.getClinicDayId() == clinicDayId)
                .sorted((a, b) -> Integer.compare(a.getPositionInQueue(), b.getPositionInQueue()))
                .collect(Collectors.toList());
    }
}
