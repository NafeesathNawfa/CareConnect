package com.careconnect.dao;

import com.careconnect.model.Patient;
import java.util.ArrayList;
import java.util.List;

public class MemoryPatientDAO implements PatientDAO {
    private final MemoryDataStore ds = MemoryDataStore.getInstance();

    @Override
    public int registerPatient(Patient patient) throws Exception {
        return ds.registerPatient(patient);
    }

    @Override
    public Patient getPatientById(int patientId) throws Exception {
        return ds.patients.get(patientId);
    }

    @Override
    public Patient getPatientByPhone(String phone) throws Exception {
        if (phone == null) return null;
        for (Patient p : ds.patients.values()) {
            if (phone.trim().equalsIgnoreCase(p.getPhone())) {
                return p;
            }
        }
        return null;
    }

    @Override
    public List<Patient> getAllPatients() throws Exception {
        List<Patient> list = new ArrayList<>(ds.patients.values());
        list.sort((a, b) -> a.getFullName().compareToIgnoreCase(b.getFullName()));
        return list;
    }
}
