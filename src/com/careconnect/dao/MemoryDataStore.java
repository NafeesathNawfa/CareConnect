package com.careconnect.dao;

import com.careconnect.model.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Stateful memory data store mirroring the exact tables, triggers, and stored procedures
 * from free_clinic schema (01_schema.sql, 02_logic.sql, 03_seed.sql, 05_extensions.sql).
 * Used when MySQL server is offline or JDBC driver is not in the runtime classpath.
 */
public class MemoryDataStore {
    private static final MemoryDataStore instance = new MemoryDataStore();
    public static MemoryDataStore getInstance() { return instance; }

    public final Map<Integer, Specialization> specializations = new ConcurrentHashMap<>();
    public final Map<Integer, Doctor> doctors = new ConcurrentHashMap<>();
    public final Map<Integer, Patient> patients = new ConcurrentHashMap<>();
    public final Map<Integer, Staff> staffMembers = new ConcurrentHashMap<>();
    public final Map<Integer, ClinicDay> clinicDays = new ConcurrentHashMap<>();
    public final Map<Integer, Slot> slots = new ConcurrentHashMap<>();
    public final Map<Integer, Appointment> appointments = new ConcurrentHashMap<>();
    public final Map<Integer, WaitlistEntry> waitlist = new ConcurrentHashMap<>();

    private final AtomicInteger nextPatientId = new AtomicInteger(10);
    private final AtomicInteger nextDoctorId = new AtomicInteger(10);
    private final AtomicInteger nextClinicDayId = new AtomicInteger(10);
    private final AtomicInteger nextSlotId = new AtomicInteger(50);
    private final AtomicInteger nextApptId = new AtomicInteger(20);
    private final AtomicInteger nextWaitlistId = new AtomicInteger(10);

    private MemoryDataStore() {
        seedInitialData();
    }

    private void seedInitialData() {
        // Specializations (from 03_seed.sql)
        specializations.put(1, new Specialization(1, "General Medicine"));
        specializations.put(2, new Specialization(2, "Pediatrics"));
        specializations.put(3, new Specialization(3, "Dentistry"));
        specializations.put(4, new Specialization(4, "Gynecology"));

        // Doctors (from 03_seed.sql)
        doctors.put(1, new Doctor(1, "Dr. Anita Rao", 1, "General Medicine", "9000000001", "anita@clinic.org", true, true));
        doctors.put(2, new Doctor(2, "Dr. Imran Khan", 2, "Pediatrics", "9000000002", "imran@clinic.org", true, true));
        doctors.put(3, new Doctor(3, "Dr. Meera Nair", 3, "Dentistry", "9000000003", "meera@clinic.org", false, true));

        // Staff (from 03_seed.sql)
        staffMembers.put(1, new Staff(1, "Clinic Admin", "admin", "admin123", "ADMIN", true));
        staffMembers.put(2, new Staff(2, "Front Desk", "reception", "reception123", "RECEPTIONIST", true));

        // Patients (from 03_seed.sql)
        patients.put(1, new Patient(1, "Ravi Kumar", LocalDate.of(1990, 5, 12), "MALE", "8000000001", "ravi@mail.com", "Nagpur"));
        patients.put(2, new Patient(2, "Sana Sheikh", LocalDate.of(2001, 11, 3), "FEMALE", "8000000002", "sana@mail.com", "Nagpur"));
        patients.put(3, new Patient(3, "Aarav Patil", LocalDate.of(2016, 2, 20), "MALE", "8000000003", null, "Nagpur"));
        patients.put(4, new Patient(4, "Kiran Das", LocalDate.of(1985, 3, 14), "OTHER", "8000000004", "kiran@mail.com", "Nagpur"));

        // Clinic Days: next 3 days from today (03_seed.sql)
        LocalDate today = LocalDate.now();
        clinicDays.put(1, new ClinicDay(1, today.plusDays(1), true, "Regular OPD"));
        clinicDays.put(2, new ClinicDay(2, today.plusDays(2), true, "Regular OPD"));
        clinicDays.put(3, new ClinicDay(3, today.plusDays(3), false, "Closed - holiday"));

        // Generate Slots (from 03_seed.sql)
        // Dr. Anita (1) on Day 1 (09:00 - 11:00, 20 min)
        createSlotRange(1, 1, LocalTime.of(9, 0), LocalTime.of(11, 0), 20);
        // Dr. Imran (2) on Day 1 (10:00 - 12:00, 30 min)
        createSlotRange(2, 1, LocalTime.of(10, 0), LocalTime.of(12, 0), 30);
        // Dr. Meera (3) on Day 2 (14:00 - 16:00, 30 min)
        createSlotRange(3, 2, LocalTime.of(14, 0), LocalTime.of(16, 0), 30);

        // Pre-book sample appointments
        bookSlotDirect(1, 1, "Routine Health Checkup");
        bookSlotDirect(2, 7, "Child Fever & Cold");
    }

    private void createSlotRange(int docId, int dayId, LocalTime start, LocalTime end, int minutes) {
        LocalTime cur = start;
        Doctor doc = doctors.get(docId);
        ClinicDay day = clinicDays.get(dayId);
        while (!cur.plusMinutes(minutes).isAfter(end)) {
            LocalTime next = cur.plusMinutes(minutes);
            int sid = nextSlotId.getAndIncrement();
            Slot slot = new Slot(sid, docId, doc.getFullName(), doc.getSpecializationName(), day.getClinicDate(), cur, next, "AVAILABLE");
            slot.setClinicDayId(dayId);
            slots.put(sid, slot);
            cur = next;
        }
    }

    private void bookSlotDirect(int patientId, int slotId, String reason) {
        Slot s = slots.get(slotId);
        if (s != null) {
            s.setStatus("BOOKED");
            int aid = nextApptId.getAndIncrement();
            Appointment appt = new Appointment(aid, patientId, slotId, "BOOKED", reason);
            appointments.put(aid, appt);
        }
    }

    // Business Methods mirroring Stored Procedures

    public synchronized int registerPatient(Patient p) throws Exception {
        if (p.getFullName() == null || p.getFullName().trim().isEmpty()) {
            throw new Exception("Patient full name is required");
        }
        if (p.getDob() == null || p.getDob().isAfter(LocalDate.now())) {
            throw new Exception("Date of birth cannot be in the future");
        }
        if (p.getPhone() == null || p.getPhone().trim().isEmpty()) {
            throw new Exception("Phone number is required");
        }
        for (Patient existing : patients.values()) {
            if (existing.getPhone().equalsIgnoreCase(p.getPhone().trim())) {
                throw new Exception("A patient with phone number " + p.getPhone() + " is already registered");
            }
        }
        int id = nextPatientId.getAndIncrement();
        p.setPatientId(id);
        patients.put(id, p);
        return id;
    }

    public synchronized int bookAppointment(int patientId, int slotId, String reason) throws Exception {
        Slot s = slots.get(slotId);
        if (s == null) {
            throw new Exception("Slot does not exist");
        }
        if (!"AVAILABLE".equalsIgnoreCase(s.getStatus())) {
            throw new Exception("Slot is not available");
        }
        ClinicDay cd = clinicDays.get(s.getClinicDayId());
        if (cd != null && !cd.isOpen()) {
            throw new Exception("Clinic is closed on that day");
        }
        Doctor doc = doctors.get(s.getDoctorId());
        if (doc != null && !doc.isActive()) {
            throw new Exception("Doctor is not active");
        }

        s.setStatus("BOOKED");
        int aid = nextApptId.getAndIncrement();
        Appointment appt = new Appointment(aid, patientId, slotId, "BOOKED", reason);
        appointments.put(aid, appt);
        return aid;
    }

    public synchronized void cancelAppointment(int appointmentId, Integer patientId) throws Exception {
        Appointment appt = appointments.get(appointmentId);
        if (appt == null) {
            throw new Exception("Appointment not found");
        }
        if (patientId != null && appt.getPatientId() != patientId) {
            throw new Exception("This appointment belongs to another patient");
        }
        if (!"BOOKED".equalsIgnoreCase(appt.getStatus())) {
            throw new Exception("Only BOOKED appointments can be cancelled");
        }

        appt.setStatus("CANCELLED");
        Slot s = slots.get(appt.getSlotId());
        if (s != null) {
            s.setStatus("AVAILABLE");

            // Waitlist auto-promotion logic from 05_extensions.sql sp_cancel_appointment
            WaitlistEntry candidate = null;
            for (WaitlistEntry we : waitlist.values()) {
                if (we.getDoctorId() == s.getDoctorId() && we.getClinicDayId() == s.getClinicDayId() && "WAITING".equals(we.getStatus())) {
                    if (candidate == null || we.getWaitlistId() < candidate.getWaitlistId()) {
                        candidate = we;
                    }
                }
            }
            if (candidate != null) {
                candidate.setStatus("BOOKED");
                s.setStatus("BOOKED");
                int newApptId = nextApptId.getAndIncrement();
                appointments.put(newApptId, new Appointment(newApptId, candidate.getPatientId(), s.getSlotId(), "BOOKED", "Booked from waitlist"));
            }
        }
    }

    public synchronized void setAppointmentOutcome(int appointmentId, String outcome) throws Exception {
        if (!"COMPLETED".equalsIgnoreCase(outcome) && !"NO_SHOW".equalsIgnoreCase(outcome)) {
            throw new Exception("Outcome must be COMPLETED or NO_SHOW");
        }
        Appointment appt = appointments.get(appointmentId);
        if (appt == null) {
            throw new Exception("Appointment not found");
        }
        if (!"BOOKED".equalsIgnoreCase(appt.getStatus())) {
            throw new Exception("Only BOOKED appointments can change outcome");
        }
        appt.setStatus(outcome.toUpperCase());
    }

    public synchronized void setClinicDay(LocalDate date, boolean isOpen, String notes) throws Exception {
        for (ClinicDay cd : clinicDays.values()) {
            if (cd.getClinicDate().equals(date)) {
                cd.setOpen(isOpen);
                cd.setNotes(notes);
                return;
            }
        }
        int id = nextClinicDayId.getAndIncrement();
        clinicDays.put(id, new ClinicDay(id, date, isOpen, notes));
    }

    public synchronized void generateSlots(int doctorId, int clinicDayId, LocalTime start, LocalTime end, int minutes) throws Exception {
        if (minutes <= 0 || !end.isAfter(start)) {
            throw new Exception("Invalid time range or slot length");
        }
        createSlotRange(doctorId, clinicDayId, start, end, minutes);
    }

    public synchronized void blockSlot(int slotId, boolean block) throws Exception {
        Slot s = slots.get(slotId);
        if (s == null) {
            throw new Exception("Slot not found");
        }
        if (block) {
            if (!"AVAILABLE".equals(s.getStatus())) {
                throw new Exception("Slot cannot be blocked because it is already " + s.getStatus());
            }
            s.setStatus("BLOCKED");
        } else {
            if (!"BLOCKED".equals(s.getStatus())) {
                throw new Exception("Slot is not currently blocked");
            }
            s.setStatus("AVAILABLE");
        }
    }

    public synchronized int joinWaitlist(int patientId, int doctorId, int clinicDayId) throws Exception {
        ClinicDay cd = clinicDays.get(clinicDayId);
        if (cd == null) throw new Exception("Clinic day does not exist");
        if (!cd.isOpen()) throw new Exception("Clinic is closed on that day");
        if (cd.getClinicDate().isBefore(LocalDate.now())) throw new Exception("Cannot join a waitlist for a past day");

        // Check duplicate
        for (WaitlistEntry we : waitlist.values()) {
            if (we.getPatientId() == patientId && we.getDoctorId() == doctorId && we.getClinicDayId() == clinicDayId && "WAITING".equals(we.getStatus())) {
                throw new Exception("You are already on this waitlist");
            }
        }

        // Count available slots
        long free = slots.values().stream().filter(s -> s.getDoctorId() == doctorId && s.getClinicDayId() == clinicDayId && "AVAILABLE".equals(s.getStatus())).count();
        if (free > 0) {
            throw new Exception("Slots are still available, book one directly");
        }

        int wid = nextWaitlistId.getAndIncrement();
        Patient p = patients.get(patientId);
        Doctor d = doctors.get(doctorId);
        int queuePos = (int) waitlist.values().stream().filter(we -> we.getDoctorId() == doctorId && we.getClinicDayId() == clinicDayId && "WAITING".equals(we.getStatus())).count() + 1;
        WaitlistEntry entry = new WaitlistEntry(wid, patientId, p != null ? p.getFullName() : "Patient #" + patientId,
                doctorId, d != null ? d.getFullName() : "Doctor #" + doctorId,
                clinicDayId, cd.getClinicDate(), "WAITING", queuePos);
        waitlist.put(wid, entry);
        return wid;
    }

    public synchronized void leaveWaitlist(int waitlistId, Integer patientId) throws Exception {
        WaitlistEntry we = waitlist.get(waitlistId);
        if (we == null || !"WAITING".equals(we.getStatus()) || (patientId != null && we.getPatientId() != patientId)) {
            throw new Exception("Waitlist entry not found or already closed");
        }
        we.setStatus("REMOVED");
    }

    public List<AppointmentDetails> getAppointmentDetailsList() {
        List<AppointmentDetails> list = new ArrayList<>();
        for (Appointment a : appointments.values()) {
            Slot s = slots.get(a.getSlotId());
            Patient p = patients.get(a.getPatientId());
            if (s != null && p != null) {
                Doctor d = doctors.get(s.getDoctorId());
                ClinicDay cd = clinicDays.get(s.getClinicDayId());
                AppointmentDetails ad = new AppointmentDetails();
                ad.setAppointmentId(a.getAppointmentId());
                ad.setStatus(a.getStatus());
                ad.setReason(a.getReason());
                ad.setPatientId(p.getPatientId());
                ad.setPatientName(p.getFullName());
                ad.setPatientPhone(p.getPhone());
                if (d != null) {
                    ad.setDoctorId(d.getDoctorId());
                    ad.setDoctorName(d.getFullName());
                    ad.setSpecialization(d.getSpecializationName());
                }
                if (cd != null) {
                    ad.setClinicDate(cd.getClinicDate());
                } else if (s.getClinicDate() != null) {
                    ad.setClinicDate(s.getClinicDate());
                }
                ad.setStartTime(s.getStartTime());
                ad.setEndTime(s.getEndTime());
                list.add(ad);
            }
        }
        return list;
    }
}
