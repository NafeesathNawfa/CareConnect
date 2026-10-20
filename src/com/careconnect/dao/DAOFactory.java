package com.careconnect.dao;

/**
 * Factory class providing DAO instances.
 * Automatically selects MySQL JDBC implementations if the database is running and reachable,
 * or the high-fidelity stateful memory implementations matching 100% of the SQL business logic
 * if MySQL is offline or not configured yet.
 */
public class DAOFactory {
    private static DAOFactory instance;

    private boolean useJdbc;
    private PatientDAO patientDAO;
    private DoctorDAO doctorDAO;
    private ClinicDayDAO clinicDayDAO;
    private SlotDAO slotDAO;
    private AppointmentDAO appointmentDAO;
    private StaffDAO staffDAO;
    private WaitlistDAO waitlistDAO;

    private DAOFactory() {
        this.useJdbc = DBConnection.isAvailable();
        initializeDAOs();
    }

    public static synchronized DAOFactory getInstance() {
        if (instance == null) {
            instance = new DAOFactory();
        }
        return instance;
    }

    private void initializeDAOs() {
        if (useJdbc) {
            patientDAO = new JdbcPatientDAO();
            doctorDAO = new JdbcDoctorDAO();
            clinicDayDAO = new JdbcClinicDayDAO();
            slotDAO = new JdbcSlotDAO();
            appointmentDAO = new JdbcAppointmentDAO();
            staffDAO = new JdbcStaffDAO();
            waitlistDAO = new JdbcWaitlistDAO();
        } else {
            patientDAO = new MemoryPatientDAO();
            doctorDAO = new MemoryDoctorDAO();
            clinicDayDAO = new MemoryClinicDayDAO();
            slotDAO = new MemorySlotDAO();
            appointmentDAO = new MemoryAppointmentDAO();
            staffDAO = new MemoryStaffDAO();
            waitlistDAO = new MemoryWaitlistDAO();
        }
    }

    public synchronized void setUseJdbc(boolean enableJdbc) {
        this.useJdbc = enableJdbc;
        initializeDAOs();
    }

    public boolean isLiveDatabaseActive() {
        return useJdbc;
    }

    public String getDataLayerStatusDescription() {
        if (useJdbc) {
            return "Data Layer: MySQL (Live Database - free_clinic)";
        } else {
            return "Data Layer: Memory Provider (Schema Simulated - MySQL Offline)";
        }
    }

    public PatientDAO getPatientDAO() { return patientDAO; }
    public DoctorDAO getDoctorDAO() { return doctorDAO; }
    public ClinicDayDAO getClinicDayDAO() { return clinicDayDAO; }
    public SlotDAO getSlotDAO() { return slotDAO; }
    public AppointmentDAO getAppointmentDAO() { return appointmentDAO; }
    public StaffDAO getStaffDAO() { return staffDAO; }
    public WaitlistDAO getWaitlistDAO() { return waitlistDAO; }
}
