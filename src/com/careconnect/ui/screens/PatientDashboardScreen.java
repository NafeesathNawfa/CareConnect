package com.careconnect.ui.screens;

import com.careconnect.dao.DAOFactory;
import com.careconnect.model.AppointmentDetails;
import com.careconnect.model.Patient;
import com.careconnect.ui.components.ConfirmationDialog;
import com.careconnect.ui.components.PageHeader;
import com.careconnect.ui.theme.*;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Patient Dashboard showing overview stats, upcoming appointments, and quick actions.
 */
public class PatientDashboardScreen extends JPanel {

    public interface DashboardActionCallback {
        void onBookAppointmentRequested();
        void onViewAppointmentsRequested();
    }

    private final Patient currentPatient;
    private final DashboardActionCallback callback;
    private final StatusBanner statusBanner;
    private final DefaultTableModel tableModel;
    private final ModernTable appointmentsTable;
    private final JLabel upcomingCountLabel;
    private final JLabel completedCountLabel;
    private final JLabel totalCountLabel;
    private List<AppointmentDetails> patientAppointments;

    public PatientDashboardScreen(Patient patient, DashboardActionCallback callback) {
        this.currentPatient = patient;
        this.callback = callback;
        setLayout(new BorderLayout());
        setBackground(UITheme.COLOR_BG_CANVAS);

        PageHeader header = new PageHeader(
                "Welcome, " + currentPatient.getFullName(),
                "Patient ID: #" + currentPatient.getPatientId() + " • Phone: " + currentPatient.getPhone()
        );

        ModernButton quickBookBtn = new ModernButton("+ Book Appointment", ModernButton.StyleType.PRIMARY);
        quickBookBtn.addActionListener(e -> {
            if (callback != null) callback.onBookAppointmentRequested();
        });
        header.addAction(quickBookBtn);

        add(header, BorderLayout.NORTH);

        // Center Area
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(20, 28, 20, 28));

        statusBanner = new StatusBanner();
        center.add(statusBanner);
        center.add(Box.createVerticalStrut(16));

        // Metric Stat Cards Row
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 20, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(1100, 100));

        upcomingCountLabel = new JLabel("0");
        completedCountLabel = new JLabel("0");
        totalCountLabel = new JLabel("0");

        statsRow.add(createMetricCard("Upcoming Booked", upcomingCountLabel, UITheme.COLOR_OCEAN_BLUE));
        statsRow.add(createMetricCard("Completed Visits", completedCountLabel, UITheme.COLOR_SUCCESS));
        statsRow.add(createMetricCard("Total Appointments", totalCountLabel, UITheme.COLOR_DEEP_BLUE));

        center.add(statsRow);
        center.add(Box.createVerticalStrut(24));

        // Upcoming Appointments Section
        JPanel tableSection = new JPanel(new BorderLayout(0, 10));
        tableSection.setBackground(UITheme.COLOR_SURFACE);
        tableSection.setBorder(new CompoundBorder(
                new LineBorder(UITheme.COLOR_BORDER, 1, false),
                new EmptyBorder(18, 20, 18, 20)
        ));

        JPanel sectionHeader = new JPanel(new BorderLayout());
        sectionHeader.setOpaque(false);

        JLabel sectionTitle = new JLabel("Your Upcoming Scheduled Appointments");
        sectionTitle.setFont(UITheme.FONT_TITLE_MEDIUM);
        sectionTitle.setForeground(UITheme.COLOR_DEEP_BLUE);

        ModernButton viewAllBtn = new ModernButton("View All Records →", ModernButton.StyleType.LINK);
        viewAllBtn.addActionListener(e -> {
            if (callback != null) callback.onViewAppointmentsRequested();
        });

        sectionHeader.add(sectionTitle, BorderLayout.WEST);
        sectionHeader.add(viewAllBtn, BorderLayout.EAST);
        tableSection.add(sectionHeader, BorderLayout.NORTH);

        // Table
        String[] columns = {"Appt ID", "Doctor", "Clinic Date", "Time Slot", "Reason", "Status", "Action"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        appointmentsTable = new ModernTable(tableModel);
        appointmentsTable.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());

        JScrollPane scrollPane = ModernTable.wrapInScrollPane(appointmentsTable);
        scrollPane.setPreferredSize(new Dimension(800, 260));
        tableSection.add(scrollPane, BorderLayout.CENTER);

        // Cancel Action Toolbar below table
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        actionRow.setOpaque(false);

        ModernButton cancelSelectedBtn = new ModernButton("Cancel Selected Appointment", ModernButton.StyleType.DANGER);
        cancelSelectedBtn.addActionListener(e -> handleCancelSelected());
        actionRow.add(cancelSelectedBtn);

        tableSection.add(actionRow, BorderLayout.SOUTH);

        center.add(tableSection);
        add(new JScrollPane(center), BorderLayout.CENTER);

        loadDashboardData();
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.COLOR_SURFACE);
        card.setBorder(new CompoundBorder(
                new LineBorder(UITheme.COLOR_BORDER, 1, false),
                new EmptyBorder(16, 20, 16, 20)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_SMALL);
        titleLbl.setForeground(UITheme.COLOR_TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLabel.setForeground(accentColor);

        card.add(titleLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(valueLabel);
        return card;
    }

    public void loadDashboardData() {
        statusBanner.clear();
        tableModel.setRowCount(0);

        try {
            patientAppointments = DAOFactory.getInstance().getAppointmentDAO().getAppointmentsByPatient(currentPatient.getPatientId());

            int upcoming = 0;
            int completed = 0;
            int total = patientAppointments.size();

            for (AppointmentDetails ad : patientAppointments) {
                if ("BOOKED".equalsIgnoreCase(ad.getStatus())) {
                    upcoming++;
                    tableModel.addRow(new Object[]{
                            "#" + ad.getAppointmentId(),
                            ad.getDoctorName() != null ? ad.getDoctorName() : "Doctor",
                            ad.getClinicDate() != null ? ad.getClinicDate().toString() : "-",
                            ad.getTimeRangeFormatted(),
                            ad.getReason() != null ? ad.getReason() : "",
                            ad.getStatus(),
                            "Cancel Eligible"
                    });
                } else if ("COMPLETED".equalsIgnoreCase(ad.getStatus())) {
                    completed++;
                }
            }

            upcomingCountLabel.setText(String.valueOf(upcoming));
            completedCountLabel.setText(String.valueOf(completed));
            totalCountLabel.setText(String.valueOf(total));

            if (upcoming == 0) {
                tableModel.addRow(new Object[]{"-", "No upcoming appointments", "-", "-", "-", "NONE", "-"});
            }

        } catch (Exception ex) {
            statusBanner.showMessage("Failed to load dashboard records: " + ex.getMessage(), StatusBanner.Type.ERROR);
        }
    }

    private void handleCancelSelected() {
        statusBanner.clear();
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow < 0) {
            statusBanner.showMessage("Please select a booked appointment in the table to cancel.", StatusBanner.Type.WARNING);
            return;
        }

        Object apptIdObj = tableModel.getValueAt(selectedRow, 0);
        if (apptIdObj == null || !apptIdObj.toString().startsWith("#")) {
            statusBanner.showMessage("Selected row is not an active appointment.", StatusBanner.Type.WARNING);
            return;
        }

        int apptId = Integer.parseInt(apptIdObj.toString().replace("#", "").trim());
        String doctor = String.valueOf(tableModel.getValueAt(selectedRow, 1));
        String date = String.valueOf(tableModel.getValueAt(selectedRow, 2));
        String time = String.valueOf(tableModel.getValueAt(selectedRow, 3));

        boolean confirmed = ConfirmationDialog.showConfirm(
                SwingUtilities.getWindowAncestor(this),
                "Confirm Appointment Cancellation",
                "Are you sure you want to cancel your appointment with <b>" + doctor + "</b> on <b>" + date + " (" + time + ")</b>?<br><br>The slot will immediately be made available for waitlisted patients.",
                "Yes, Cancel Appointment",
                true
        );

        if (confirmed) {
            try {
                DAOFactory.getInstance().getAppointmentDAO().cancelAppointment(apptId, currentPatient.getPatientId());
                statusBanner.showMessage("Appointment #" + apptId + " was successfully cancelled.", StatusBanner.Type.SUCCESS);
                loadDashboardData();
            } catch (Exception ex) {
                statusBanner.showMessage("Cancellation error: " + ex.getMessage(), StatusBanner.Type.ERROR);
            }
        }
    }
}
