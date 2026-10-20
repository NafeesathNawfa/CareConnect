package com.careconnect.dao;

import com.careconnect.model.Slot;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MemorySlotDAO implements SlotDAO {
    private final MemoryDataStore ds = MemoryDataStore.getInstance();

    @Override
    public List<Slot> getAvailableSlots() throws Exception {
        return ds.slots.values().stream()
                .filter(s -> "AVAILABLE".equalsIgnoreCase(s.getStatus()))
                .sorted((a, b) -> {
                    int c = a.getClinicDate().compareTo(b.getClinicDate());
                    return c != 0 ? c : a.getStartTime().compareTo(b.getStartTime());
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Slot> getAvailableSlots(Integer doctorId, LocalDate date) throws Exception {
        return ds.slots.values().stream()
                .filter(s -> "AVAILABLE".equalsIgnoreCase(s.getStatus()))
                .filter(s -> doctorId == null || s.getDoctorId() == doctorId)
                .filter(s -> date == null || s.getClinicDate().equals(date))
                .sorted((a, b) -> {
                    int c = a.getClinicDate().compareTo(b.getClinicDate());
                    return c != 0 ? c : a.getStartTime().compareTo(b.getStartTime());
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Slot> getSlotsByDoctorAndDay(int doctorId, int clinicDayId) throws Exception {
        return ds.slots.values().stream()
                .filter(s -> s.getDoctorId() == doctorId && s.getClinicDayId() == clinicDayId)
                .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
                .collect(Collectors.toList());
    }

    @Override
    public Slot getSlotById(int slotId) throws Exception {
        return ds.slots.get(slotId);
    }

    @Override
    public void generateSlots(int doctorId, int clinicDayId, LocalTime start, LocalTime end, int slotMinutes) throws Exception {
        ds.generateSlots(doctorId, clinicDayId, start, end, slotMinutes);
    }

    @Override
    public void blockSlot(int slotId, boolean block) throws Exception {
        ds.blockSlot(slotId, block);
    }
}
