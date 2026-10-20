package com.careconnect.dao;

import com.careconnect.model.ClinicDay;
import java.time.LocalDate;
import java.util.List;

/**
 * Data Access Object interface for clinic operation dates.
 */
public interface ClinicDayDAO {
    List<ClinicDay> getAllClinicDays() throws Exception;
    List<ClinicDay> getUpcomingClinicDays() throws Exception;
    ClinicDay getClinicDayById(int clinicDayId) throws Exception;
    ClinicDay getClinicDayByDate(LocalDate date) throws Exception;
    void setClinicDay(LocalDate date, boolean isOpen, String notes) throws Exception;
}
