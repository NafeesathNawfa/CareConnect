package com.careconnect.ui.screens;

import com.careconnect.model.Patient;
import com.careconnect.model.Staff;
import com.careconnect.ui.components.SidebarNav;
import com.careconnect.ui.theme.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * Main application frame hosting the navigation sidebar and dynamic screen container.
 */
public class MainFrame extends JFrame implements SidebarNav.NavListener, WelcomeLoginScreen.LoginCallback, PatientRegistrationScreen.RegistrationCallback {

    private final JPanel mainContainer;
    private final CardLayout cardLayout;
    private SidebarNav sidebarNav;

    private Patient loggedInPatient;
    private Staff loggedInStaff;

    // Active Screen references
    private WelcomeLoginScreen welcomeScreen;
    private PatientRegistrationScreen registrationScreen;
    private PatientDashboardScreen patientDashboardScreen;
    private BookAppointmentScreen bookAppointmentScreen;
    private PatientAppointmentsScreen patientAppointmentsScreen;
    private DoctorScheduleScreen doctorScheduleScreen;
    private AdminAppointmentsScreen adminAppointmentsScreen;
    private AdminManagementScreen adminManagementScreen;

    public MainFrame() {
        super("CareConnect - Clinic Appointment Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(960, 640));
        setLocationRelativeTo(null);

        // Apply global UI settings
        getContentPane().setBackground(UITheme.COLOR_BG_CANVAS);
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        mainContainer.setBackground(UITheme.COLOR_BG_CANVAS);

        sidebarNav = new SidebarNav(this);

        initInitialScreens();
        showScreen("welcome");
    }

    private void initInitialScreens() {
        welcomeScreen = new WelcomeLoginScreen(this);
        registrationScreen = new PatientRegistrationScreen(this);

        mainContainer.add(welcomeScreen, "welcome");
        mainContainer.add(registrationScreen, "register");

        add(mainContainer, BorderLayout.CENTER);
    }

    private void showScreen(String name) {
        cardLayout.show(mainContainer, name);
        mainContainer.revalidate();
        mainContainer.repaint();
    }

    // ==========================================
    // LOGIN & REGISTRATION CALLBACKS
    // ==========================================

    @Override
    public void onPatientLogin(Patient patient) {
        this.loggedInPatient = patient;
        this.loggedInStaff = null;

        getContentPane().add(sidebarNav, BorderLayout.WEST);
        sidebarNav.configureForPatient(patient);
        sidebarNav.setActive("patient_dashboard");

        // Initialize patient screens
        patientDashboardScreen = new PatientDashboardScreen(patient, new PatientDashboardScreen.DashboardActionCallback() {
            @Override
            public void onBookAppointmentRequested() {
                onNavSelected("book_appointment");
            }

            @Override
            public void onViewAppointmentsRequested() {
                onNavSelected("my_appointments");
            }
        });

        bookAppointmentScreen = new BookAppointmentScreen(patient, () -> {
            // When appointment is booked, refresh dashboard and appointments
            if (patientAppointmentsScreen != null) patientAppointmentsScreen.loadAppointments();
            if (patientDashboardScreen != null) patientDashboardScreen.loadDashboardData();
        });

        patientAppointmentsScreen = new PatientAppointmentsScreen(patient);

        mainContainer.add(patientDashboardScreen, "patient_dashboard");
        mainContainer.add(bookAppointmentScreen, "book_appointment");
        mainContainer.add(patientAppointmentsScreen, "my_appointments");

        showScreen("patient_dashboard");
    }

    @Override
    public void onStaffLogin(Staff staff) {
        this.loggedInStaff = staff;
        this.loggedInPatient = null;

        getContentPane().add(sidebarNav, BorderLayout.WEST);
        sidebarNav.configureForStaff(staff);
        sidebarNav.setActive("admin_appointments");

        // Initialize staff screens
        adminAppointmentsScreen = new AdminAppointmentsScreen();
        doctorScheduleScreen = new DoctorScheduleScreen();
        adminManagementScreen = new AdminManagementScreen("manage_doctors");

        mainContainer.add(adminAppointmentsScreen, "admin_appointments");
        mainContainer.add(doctorScheduleScreen, "doctor_schedule");
        mainContainer.add(adminManagementScreen, "admin_management");

        showScreen("admin_appointments");
    }

    @Override
    public void onOpenPatientRegistration() {
        getContentPane().remove(sidebarNav);
        showScreen("register");
    }

    @Override
    public void onRegistrationSuccess(Patient registeredPatient) {
        // Auto-login to patient dashboard upon registration
        onPatientLogin(registeredPatient);
    }

    @Override
    public void onBackToLogin() {
        showScreen("welcome");
    }

    // ==========================================
    // SIDEBAR NAVIGATION CALLBACKS
    // ==========================================

    @Override
    public void onNavSelected(String screenKey) {
        sidebarNav.setActive(screenKey);

        switch (screenKey) {
            case "patient_dashboard":
                if (patientDashboardScreen != null) patientDashboardScreen.loadDashboardData();
                showScreen("patient_dashboard");
                break;
            case "book_appointment":
                if (bookAppointmentScreen != null) bookAppointmentScreen.loadAvailableSlots();
                showScreen("book_appointment");
                break;
            case "my_appointments":
                if (patientAppointmentsScreen != null) patientAppointmentsScreen.loadAppointments();
                showScreen("my_appointments");
                break;
            case "admin_appointments":
                if (adminAppointmentsScreen != null) adminAppointmentsScreen.loadAppointments();
                showScreen("admin_appointments");
                break;
            case "doctor_schedule":
                if (doctorScheduleScreen != null) doctorScheduleScreen.loadSchedule();
                showScreen("doctor_schedule");
                break;
            case "manage_doctors":
            case "manage_days":
            case "manage_waitlist":
                if (adminManagementScreen != null) {
                    adminManagementScreen.selectTab(screenKey);
                }
                showScreen("admin_management");
                break;
            default:
                break;
        }
    }

    @Override
    public void onLogout() {
        loggedInPatient = null;
        loggedInStaff = null;
        getContentPane().remove(sidebarNav);

        // Recreate welcome screen to reset any stale inputs
        welcomeScreen = new WelcomeLoginScreen(this);
        mainContainer.add(welcomeScreen, "welcome");
        showScreen("welcome");

        getContentPane().revalidate();
        getContentPane().repaint();
    }
}
