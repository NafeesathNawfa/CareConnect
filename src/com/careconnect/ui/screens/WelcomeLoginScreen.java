package com.careconnect.ui.screens;

import com.careconnect.dao.DAOFactory;
import com.careconnect.model.Patient;
import com.careconnect.model.Staff;
import com.careconnect.ui.theme.*;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Welcome and Login Screen with role-based navigation (Patient Portal vs Staff/Admin Portal).
 */
public class WelcomeLoginScreen extends JPanel {

    public interface LoginCallback {
        void onPatientLogin(Patient patient);
        void onStaffLogin(Staff staff);
        void onOpenPatientRegistration();
    }

    private final LoginCallback callback;
    private final StatusBanner statusBanner;
    private final ModernTextField patientPhoneField;
    private final ModernTextField staffUsernameField;
    private final ModernPasswordField staffPasswordField;

    public WelcomeLoginScreen(LoginCallback callback) {
        this.callback = callback;
        setLayout(new BorderLayout());
        setBackground(UITheme.COLOR_BG_CANVAS);

        // Top Brand Banner
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(UITheme.COLOR_NAVY);
        headerPanel.setBorder(new EmptyBorder(36, 40, 36, 40));

        JLabel titleLabel = new JLabel("CareConnect Free Clinic");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(UITheme.COLOR_TEXT_ON_DARK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel("Community Healthcare & Outpatient Appointment Management");
        subLabel.setFont(UITheme.FONT_DESC_ITALIC);
        subLabel.setForeground(UITheme.COLOR_SKY_BLUE);
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(8));
        headerPanel.add(subLabel);

        add(headerPanel, BorderLayout.NORTH);

        // Center Content Area (Cards)
        JPanel centerContainer = new JPanel();
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));
        centerContainer.setOpaque(false);
        centerContainer.setBorder(new EmptyBorder(24, 40, 24, 40));

        statusBanner = new StatusBanner();
        statusBanner.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerContainer.add(statusBanner);
        centerContainer.add(Box.createVerticalStrut(16));

        // Two Cards Side by Side
        JPanel cardsRow = new JPanel(new GridLayout(1, 2, 28, 0));
        cardsRow.setOpaque(false);
        cardsRow.setMaximumSize(new Dimension(860, 420));

        // Card 1: Patient Portal
        JPanel patientCard = createCardPanel("Patient Portal", "Access your appointments, check clinic days, or register as a new patient.");
        JPanel patientForm = new JPanel();
        patientForm.setLayout(new BoxLayout(patientForm, BoxLayout.Y_AXIS));
        patientForm.setOpaque(false);

        JLabel phoneLbl = new JLabel("Enter Registered Phone Number:");
        phoneLbl.setFont(UITheme.FONT_BODY_BOLD);
        phoneLbl.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        patientPhoneField = new ModernTextField("e.g. 8000000001");

        ModernButton patientLoginBtn = new ModernButton("Enter Patient Portal", ModernButton.StyleType.PRIMARY);
        patientLoginBtn.addActionListener(e -> handlePatientLogin());

        JLabel orLabel = new JLabel("― or ―");
        orLabel.setFont(UITheme.FONT_SMALL);
        orLabel.setForeground(UITheme.COLOR_TEXT_MUTED);
        orLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        ModernButton registerBtn = new ModernButton("Register as New Patient", ModernButton.StyleType.SECONDARY);
        registerBtn.addActionListener(e -> {
            if (callback != null) callback.onOpenPatientRegistration();
        });

        // Quick demo hint
        JLabel demoHint1 = new JLabel("Demo patients: 8000000001 (Ravi), 8000000002 (Sana)");
        demoHint1.setFont(UITheme.FONT_SMALL);
        demoHint1.setForeground(UITheme.COLOR_TEXT_MUTED);

        patientForm.add(phoneLbl);
        patientForm.add(Box.createVerticalStrut(6));
        patientForm.add(patientPhoneField);
        patientForm.add(Box.createVerticalStrut(14));
        patientForm.add(patientLoginBtn);
        patientForm.add(Box.createVerticalStrut(12));
        patientForm.add(orLabel);
        patientForm.add(Box.createVerticalStrut(12));
        patientForm.add(registerBtn);
        patientForm.add(Box.createVerticalStrut(12));
        patientForm.add(demoHint1);

        patientCard.add(patientForm, BorderLayout.CENTER);

        // Card 2: Staff / Admin Portal
        JPanel staffCard = createCardPanel("Staff & Admin Portal", "Authorized login for doctors, clinic administrators, and receptionists.");
        JPanel staffForm = new JPanel();
        staffForm.setLayout(new BoxLayout(staffForm, BoxLayout.Y_AXIS));
        staffForm.setOpaque(false);

        JLabel userLbl = new JLabel("Username:");
        userLbl.setFont(UITheme.FONT_BODY_BOLD);
        userLbl.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        staffUsernameField = new ModernTextField("e.g. admin or reception");

        JLabel passLbl = new JLabel("Password:");
        passLbl.setFont(UITheme.FONT_BODY_BOLD);
        passLbl.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        staffPasswordField = new ModernPasswordField("password");

        ModernButton staffLoginBtn = new ModernButton("Staff Sign In", ModernButton.StyleType.PRIMARY);
        staffLoginBtn.addActionListener(e -> handleStaffLogin());

        JLabel demoHint2 = new JLabel("Default logins: 'admin' / 'admin123' | 'reception' / 'reception123'");
        demoHint2.setFont(UITheme.FONT_SMALL);
        demoHint2.setForeground(UITheme.COLOR_TEXT_MUTED);

        staffForm.add(userLbl);
        staffForm.add(Box.createVerticalStrut(6));
        staffForm.add(staffUsernameField);
        staffForm.add(Box.createVerticalStrut(10));
        staffForm.add(passLbl);
        staffForm.add(Box.createVerticalStrut(6));
        staffForm.add(staffPasswordField);
        staffForm.add(Box.createVerticalStrut(16));
        staffForm.add(staffLoginBtn);
        staffForm.add(Box.createVerticalStrut(14));
        staffForm.add(demoHint2);

        staffCard.add(staffForm, BorderLayout.CENTER);

        cardsRow.add(patientCard);
        cardsRow.add(staffCard);

        centerContainer.add(cardsRow);
        add(centerContainer, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(UITheme.COLOR_SURFACE);
        footer.setBorder(new CompoundBorder(
                new LineBorder(UITheme.COLOR_BORDER, 1, false),
                new EmptyBorder(10, 20, 10, 20)
        ));
        JLabel footerLabel = new JLabel("CareConnect Clinical Information System • " + DAOFactory.getInstance().getDataLayerStatusDescription());
        footerLabel.setFont(UITheme.FONT_SMALL);
        footerLabel.setForeground(UITheme.COLOR_TEXT_MUTED);
        footer.add(footerLabel);
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel createCardPanel(String title, String subtitle) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UITheme.COLOR_SURFACE);
        card.setBorder(new CompoundBorder(
                new LineBorder(UITheme.COLOR_BORDER, 1, false),
                new EmptyBorder(22, 24, 22, 24)
        ));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_TITLE_MEDIUM);
        titleLbl.setForeground(UITheme.COLOR_DEEP_BLUE);

        JLabel subLbl = new JLabel("<html><body style='width: 320px;'>" + subtitle + "</body></html>");
        subLbl.setFont(UITheme.FONT_SMALL);
        subLbl.setForeground(UITheme.COLOR_TEXT_MUTED);

        header.add(titleLbl);
        header.add(Box.createVerticalStrut(4));
        header.add(subLbl);

        card.add(header, BorderLayout.NORTH);
        return card;
    }

    private void handlePatientLogin() {
        statusBanner.clear();
        String phone = patientPhoneField.getText().trim();
        if (phone.isEmpty()) {
            statusBanner.showMessage("Please enter your registered phone number.", StatusBanner.Type.WARNING);
            patientPhoneField.requestFocus();
            return;
        }

        try {
            Patient patient = DAOFactory.getInstance().getPatientDAO().getPatientByPhone(phone);
            if (patient != null) {
                if (callback != null) callback.onPatientLogin(patient);
            } else {
                statusBanner.showMessage("No registered patient found with phone number " + phone + ". Please click 'Register as New Patient'.", StatusBanner.Type.ERROR);
            }
        } catch (Exception ex) {
            statusBanner.showMessage("Error verifying patient: " + ex.getMessage(), StatusBanner.Type.ERROR);
        }
    }

    private void handleStaffLogin() {
        statusBanner.clear();
        String username = staffUsernameField.getText().trim();
        String password = new String(staffPasswordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            statusBanner.showMessage("Please enter both username and password.", StatusBanner.Type.WARNING);
            return;
        }

        try {
            Staff staff = DAOFactory.getInstance().getStaffDAO().authenticate(username, password);
            if (staff != null) {
                if (callback != null) callback.onStaffLogin(staff);
            } else {
                statusBanner.showMessage("Invalid staff credentials. Try username 'admin' and password 'admin123'.", StatusBanner.Type.ERROR);
            }
        } catch (Exception ex) {
            statusBanner.showMessage("Authentication error: " + ex.getMessage(), StatusBanner.Type.ERROR);
        }
    }
}
