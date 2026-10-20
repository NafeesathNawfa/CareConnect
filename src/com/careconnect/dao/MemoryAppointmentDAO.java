package com.careconnect.dao;

import com.careconnect.model.AppointmentDetails;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MemoryAppointmentDAO implements AppointmentDAO {
    private final MemoryDataStore ds = MemoryDataStore.getInstance();

    @Override
    public int bookAppointment(int patientId, int slotId, String reason) throws Exception {
        return ds.bookAppointment(patientId, slotId, reason);
    }

    @Override
    public void cancelAppointment(int appointmentId, Integer patientId) throws Exception {
        ds.cancelAppointment(appointmentId, patientId);
    }

    @Override
    public void setAppointmentOutcome(int appointmentId, String outcome) throws Exception {
        ds.setAppointmentOutcome(appointmentId, outcome);
    }

    @Override
    public List<AppointmentDetails> getAppointmentsByPatient(int patientId) throws Exception {
        return ds.getAppointmentDetailsList().stream()
                .filter(ad -> ad.getPatientId() == patientId)
                .sorted((a, b) -> {
                    int c = b.getClinicDate().compareTo(a.getClinicDate());
                    return c != 0 ? c : b.getStartTime().compareTo(a.getStartTime());
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentDetails> getTodaysSchedule() throws Exception {
        LocalDate today = LocalDate.now();
        return ds.getAppointmentDetailsList().stream()
                .filter(ad -> ad.getClinicDate().equals(today) && "BOOKED".equalsIgnoreCase(ad.getStatus()))
                .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentDetails> getScheduleByDoctor(int doctorId, LocalDate date) throws Exception {
        return ds.getAppointmentDetailsList().stream()
                .filter(ad -> ad.getDoctorId() == doctorId)
                .filter(ad -> date == null || ad.getClinicDate().equals(date))
                .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentDetails> getAllAppointments(String statusFilter, Integer doctorId, String search) throws Exception {
        return ds.getAppointmentDetailsList().stream()
                .filter(ad -> {
                    if (statusFilter != null && !statusFilter.equalsIgnoreCase("ALL") && !statusFilter.trim().isEmpty()) {
                        if (!statusFilter.equalsIgnoreCase(ad.getStatus())) return false;
                    }
                    if (doctorId != null && doctorId > 0) {
                        if (ad.getDoctorId() != doctorId) return false;
                    }
                    if (search != null && !search.trim().isEmpty()) {
                        String s = search.trim().toLowerCase();
                        boolean matchName = ad.getPatientName() != null && ad.getPatientName().toLowerCase().contains(s);
                        boolean matchPhone = ad.getPatientPhone() != null && ad.getPatientPhone().contains(s);
                        boolean matchDoc = ad.getDoctorName() != null && ad.getDoctorName().toLowerCase().contains(s);
                        if (!matchName && !matchPhone && !matchDoc) return false;
                    }
                    return true;
                })
                .sorted((a, b) -> {
                    int c = b.getClinicDate().compareTo(a.getClinicDate());
                    return c != 0 ? c : b.getStartTime().compareTo(a.getStartTime());
                })
                .collect(Collectors.toList());
    }

    @Override
    public AppointmentDetails getAppointmentDetailsById(int appointmentId) throws Exception {
        for (AppointmentDetails ad : ds.getAppointmentDetailsList()) {
            if (ad.getAppointmentId() == appointmentId) {
                return ad;
            }
        }
        return null;
    }
}
