package com.careconnect.dao;

import com.careconnect.model.Patient;
import java.util.List;

/**
 * Data Access Object interface for Patient entities.
 * Interacts with patient table and sp_register_patient stored procedure.
 */
public interface PatientDAO {
    /**
     * Registers a new patient in the clinic database.
     * @param patient patient details
     * @return newly generated patient_id
     * @throws Exception on validation or database failure
     */
    int registerPatient(Patient patient) throws Exception;

    /**
     * Retrieves a patient by primary key ID.
     */
    Patient getPatientById(int patientId) throws Exception;

    /**
     * Retrieves a patient by phone number (unique identifier).
     */
    Patient getPatientByPhone(String phone) throws Exception;

    /**
     * Lists all registered patients.
     */
    List<Patient> getAllPatients() throws Exception;
}
