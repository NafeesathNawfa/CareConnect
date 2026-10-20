package com.careconnect.dao;

import com.careconnect.model.Doctor;
import com.careconnect.model.Specialization;
import java.util.List;

/**
 * Data Access Object interface for Doctor entities and Specializations.
 */
public interface DoctorDAO {
    List<Doctor> getAllDoctors() throws Exception;
    List<Doctor> getActiveDoctors() throws Exception;
    Doctor getDoctorById(int doctorId) throws Exception;
    int addDoctor(Doctor doctor) throws Exception;
    boolean updateDoctor(Doctor doctor) throws Exception;
    boolean toggleDoctorActive(int doctorId, boolean active) throws Exception;
    List<Specialization> getAllSpecializations() throws Exception;
}
