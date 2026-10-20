package com.careconnect.dao;

import com.careconnect.model.ClinicDay;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MemoryClinicDayDAO implements ClinicDayDAO {
    private final MemoryDataStore ds = MemoryDataStore.getInstance();

    @Override
    public List<ClinicDay> getAllClinicDays() throws Exception {
        return ds.clinicDays.values().stream()
                .sorted((a, b) -> a.getClinicDate().compareTo(b.getClinicDate()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ClinicDay> getUpcomingClinicDays() throws Exception {
        LocalDate today = LocalDate.now();
        return ds.clinicDays.values().stream()
                .filter(cd -> !cd.getClinicDate().isBefore(today))
                .sorted((a, b) -> a.getClinicDate().compareTo(b.getClinicDate()))
                .collect(Collectors.toList());
    }

    @Override
    public ClinicDay getClinicDayById(int clinicDayId) throws Exception {
        return ds.clinicDays.get(clinicDayId);
    }

    @Override
    public ClinicDay getClinicDayByDate(LocalDate date) throws Exception {
        for (ClinicDay cd : ds.clinicDays.values()) {
            if (cd.getClinicDate().equals(date)) {
                return cd;
            }
        }
        return null;
    }

    @Override
    public void setClinicDay(LocalDate date, boolean isOpen, String notes) throws Exception {
        ds.setClinicDay(date, isOpen, notes);
    }
}
