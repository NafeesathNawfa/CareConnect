package com.careconnect.dao;

import com.careconnect.model.Slot;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Data Access Object interface for appointment slots.
 * Backed by slot table, v_available_slots view, and sp_generate_slots / sp_block_slot.
 */
public interface SlotDAO {
    List<Slot> getAvailableSlots() throws Exception;
    List<Slot> getAvailableSlots(Integer doctorId, LocalDate date) throws Exception;
    List<Slot> getSlotsByDoctorAndDay(int doctorId, int clinicDayId) throws Exception;
    Slot getSlotById(int slotId) throws Exception;
    void generateSlots(int doctorId, int clinicDayId, LocalTime start, LocalTime end, int slotMinutes) throws Exception;
    void blockSlot(int slotId, boolean block) throws Exception;
}
