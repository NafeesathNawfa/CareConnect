package com.careconnect.dao;

import com.careconnect.model.Doctor;
import com.careconnect.model.Specialization;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MemoryDoctorDAO implements DoctorDAO {
    private final MemoryDataStore ds = MemoryDataStore.getInstance();

    @Override
    public List<Doctor> getAllDoctors() throws Exception {
        return ds.doctors.values().stream()
                .sorted((a, b) -> a.getFullName().compareToIgnoreCase(b.getFullName()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Doctor> getActiveDoctors() throws Exception {
        return ds.doctors.values().stream()
                .filter(Doctor::isActive)
                .sorted((a, b) -> a.getFullName().compareToIgnoreCase(b.getFullName()))
                .collect(Collectors.toList());
    }

    @Override
    public Doctor getDoctorById(int doctorId) throws Exception {
        return ds.doctors.get(doctorId);
    }

    @Override
    public int addDoctor(Doctor doctor) throws Exception {
        int id = ds.doctors.keySet().stream().max(Integer::compareTo).orElse(0) + 1;
        doctor.setDoctorId(id);
        Specialization sp = ds.specializations.get(doctor.getSpecializationId());
        if (sp != null) {
            doctor.setSpecializationName(sp.getName());
        }
        ds.doctors.put(id, doctor);
        return id;
    }

    @Override
    public boolean updateDoctor(Doctor doctor) throws Exception {
        if (!ds.doctors.containsKey(doctor.getDoctorId())) return false;
        Specialization sp = ds.specializations.get(doctor.getSpecializationId());
        if (sp != null) {
            doctor.setSpecializationName(sp.getName());
        }
        ds.doctors.put(doctor.getDoctorId(), doctor);
        return true;
    }

    @Override
    public boolean toggleDoctorActive(int doctorId, boolean active) throws Exception {
        Doctor doc = ds.doctors.get(doctorId);
        if (doc == null) return false;
        doc.setActive(active);
        return true;
    }

    @Override
    public List<Specialization> getAllSpecializations() throws Exception {
        return new ArrayList<>(ds.specializations.values());
    }
}
