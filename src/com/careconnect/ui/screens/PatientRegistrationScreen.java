package com.careconnect.ui.screens;

import com.careconnect.dao.DAOFactory;
import com.careconnect.model.Patient;
import com.careconnect.ui.components.PageHeader;
import com.careconnect.ui.theme.*;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Patient Registration screen.
 * Implements thorough validation and calls sp_register_patient through PatientDAO.
 */
public class PatientRegistrationScreen extends JPanel {

    public interface RegistrationCallback {
        void onRegistrationSuccess(Patient registeredPatient);
        void onBackToLogin();
    }

    private final RegistrationCallback callback;
    private final StatusBanner statusBanner;

    private final ModernTextField nameField;
    private final ModernTextField dobField;
    private final ModernComboBox<String> genderCombo;
    private final ModernTextField phoneField;
    private final ModernTextField emailField;
    private final ModernTextField addressField;

    public PatientRegistrationScreen(RegistrationCallback callback) {
        this.callback = callback;
        setLayout(new BorderLayout());
        setBackground(UITheme.COLOR_BG_CANVAS);

        PageHeader header = new PageHeader("New Patient Registration", "Enter your medical record details to create a clinic account");
        ModernButton backBtn = new ModernButton("← Back to Login", ModernButton.StyleType.SECONDARY);
        backBtn.addActionListener(e -> {
            if (callback != null) callback.onBackToLogin();
        });
        header.addAction(backBtn);
        add(header, BorderLayout.NORTH);

        // Center Form
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(20, 40, 30, 40));

        statusBanner = new StatusBanner();
        statusBanner.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(statusBanner);
        center.add(Box.createVerticalStrut(14));

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(UITheme.COLOR_SURFACE);
        formCard.setBorder(new CompoundBorder(
                new LineBorder(UITheme.COLOR_BORDER, 1, false),
                new EmptyBorder(26, 32, 26, 32)
        ));
        formCard.setMaximumSize(new Dimension(680, 520));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 8, 6, 8);

        nameField = new ModernTextField("Full Legal Name (e.g. Maya Sharma)");
        dobField = new ModernTextField("YYYY-MM-DD (e.g. 1995-08-20)");
        genderCombo = new ModernComboBox<>(new String[]{"FEMALE", "MALE", "OTHER"});
        phoneField = new ModernTextField("10-digit Phone Number (e.g. 9876543210)");
        emailField = new ModernTextField("Email Address (Optional)");
        addressField = new ModernTextField("Residential City / Address");

        int row = 0;
        addFormRow(formCard, gbc, row++, "Full Name *:", nameField);
        addFormRow(formCard, gbc, row++, "Date of Birth (YYYY-MM-DD) *:", dobField);
        addFormRow(formCard, gbc, row++, "Gender *:", genderCombo);
        addFormRow(formCard, gbc, row++, "Phone Number (Primary ID) *:", phoneField);
        addFormRow(formCard, gbc, row++, "Email Address:", emailField);
        addFormRow(formCard, gbc, row++, "Address:", addressField);

        // Button row
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.insets = new Insets(18, 8, 8, 8);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        ModernButton clearBtn = new ModernButton("Reset Form", ModernButton.StyleType.SECONDARY);
        clearBtn.addActionListener(e -> resetForm());

        ModernButton submitBtn = new ModernButton("Complete Registration", ModernButton.StyleType.PRIMARY);
        submitBtn.addActionListener(e -> handleSubmit());

        btnPanel.add(clearBtn);
        btnPanel.add(submitBtn);
        formCard.add(btnPanel, gbc);

        center.add(formCard);
        add(new JScrollPane(center), BorderLayout.CENTER);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent input) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UITheme.FONT_BODY_BOLD);
        lbl.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(input, gbc);
    }

    private void resetForm() {
        nameField.setText("");
        dobField.setText("");
        genderCombo.setSelectedIndex(0);
        phoneField.setText("");
        emailField.setText("");
        addressField.setText("");
        statusBanner.clear();
    }

    private void handleSubmit() {
        statusBanner.clear();

        String name = nameField.getText().trim();
        String dobStr = dobField.getText().trim();
        String gender = (String) genderCombo.getSelectedItem();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String address = addressField.getText().trim();

        if (name.isEmpty()) {
            statusBanner.showMessage("Full Name is required.", StatusBanner.Type.ERROR);
            nameField.requestFocus();
            return;
        }

        LocalDate dob;
        try {
            dob = LocalDate.parse(dobStr, DateTimeFormatter.ISO_LOCAL_DATE);
            if (dob.isAfter(LocalDate.now())) {
                statusBanner.showMessage("Date of birth cannot be in the future.", StatusBanner.Type.ERROR);
                dobField.requestFocus();
                return;
            }
        } catch (DateTimeParseException ex) {
            statusBanner.showMessage("Invalid Date of Birth format. Please use YYYY-MM-DD (e.g. 1995-04-12).", StatusBanner.Type.ERROR);
            dobField.requestFocus();
            return;
        }

        if (phone.isEmpty() || !phone.matches("^[0-9+ -]{7,15}$")) {
            statusBanner.showMessage("Please provide a valid phone number (digits only, 7-15 chars).", StatusBanner.Type.ERROR);
            phoneField.requestFocus();
            return;
        }

        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            statusBanner.showMessage("Invalid email format.", StatusBanner.Type.ERROR);
            emailField.requestFocus();
            return;
        }

        try {
            Patient newPatient = new Patient(0, name, dob, gender, phone, email.isEmpty() ? null : email, address.isEmpty() ? null : address);
            int newId = DAOFactory.getInstance().getPatientDAO().registerPatient(newPatient);
            newPatient.setPatientId(newId);

            JOptionPane.showMessageDialog(this,
                    "Registration successful!\nYour Patient ID is: #" + newId + "\nRegistered Phone: " + phone,
                    "Registration Confirmed",
                    JOptionPane.INFORMATION_MESSAGE);

            if (callback != null) {
                callback.onRegistrationSuccess(newPatient);
            }
        } catch (Exception ex) {
            statusBanner.showMessage("Registration failed: " + ex.getMessage(), StatusBanner.Type.ERROR);
        }
    }
}
