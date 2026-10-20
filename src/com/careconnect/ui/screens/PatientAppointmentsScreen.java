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
import java.util.ArrayList;
import java.util.List;

/**
 * Screen displaying patient's appointments with status filters and cancellation action.
 */
public class PatientAppointmentsScreen extends JPanel {

    private final Patient currentPatient;
    private final StatusBanner statusBanner;
    private final ModernTable table;
    private final DefaultTableModel tableModel;
    private final ModernComboBox<String> statusFilterCombo;
    private List<AppointmentDetails> allAppointments = new ArrayList<>();

    public PatientAppointmentsScreen(Patient patient) {
        this.currentPatient = patient;
        setLayout(new BorderLayout());
        setBackground(UITheme.COLOR_BG_CANVAS);

        PageHeader header = new PageHeader("My Appointments", "View your consultation history and manage active bookings");
        ModernButton refreshBtn = new ModernButton("Refresh List", ModernButton.StyleType.SECONDARY);
        refreshBtn.addActionListener(e -> loadAppointments());
        header.addAction(refreshBtn);
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(20, 28, 20, 28));

        statusBanner = new StatusBanner();
        center.add(statusBanner);
        center.add(Box.createVerticalStrut(14));

        // Filter Bar & Card
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(UITheme.COLOR_SURFACE);
        card.setBorder(new CompoundBorder(
                new LineBorder(UITheme.COLOR_BORDER, 1, false),
                new EmptyBorder(18, 20, 18, 20)
        ));

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filterBar.setOpaque(false);

        JLabel filterLbl = new JLabel("Filter Status:");
        filterLbl.setFont(UITheme.FONT_BODY_BOLD);
        statusFilterCombo = new ModernComboBox<>(new String[]{"ALL", "BOOKED", "COMPLETED", "CANCELLED", "NO_SHOW"});
        statusFilterCombo.addActionListener(e -> applyFilter());

        filterBar.add(filterLbl);
        filterBar.add(statusFilterCombo);

        card.add(filterBar, BorderLayout.NORTH);

        // Table
        String[] columns = {"Appt ID", "Doctor Name", "Clinic Date", "Time Window", "Visit Reason", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new ModernTable(tableModel);
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = ModernTable.wrapInScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(850, 360));
        card.add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);

        JLabel ruleNotice = new JLabel("Note: In accordance with clinic policy, only BOOKED appointments can be cancelled.");
        ruleNotice.setFont(UITheme.FONT_SMALL);
        ruleNotice.setForeground(UITheme.COLOR_TEXT_MUTED);

        ModernButton cancelBtn = new ModernButton("Cancel Selected Appointment", ModernButton.StyleType.DANGER);
        cancelBtn.addActionListener(e -> handleCancelAppointment());

        bottomBar.add(ruleNotice, BorderLayout.WEST);
        bottomBar.add(cancelBtn, BorderLayout.EAST);

        card.add(bottomBar, BorderLayout.SOUTH);

        center.add(card);
        add(new JScrollPane(center), BorderLayout.CENTER);

        loadAppointments();
    }

    public void loadAppointments() {
        statusBanner.clear();
        try {
            allAppointments = DAOFactory.getInstance().getAppointmentDAO().getAppointmentsByPatient(currentPatient.getPatientId());
            applyFilter();
        } catch (Exception ex) {
            statusBanner.showMessage("Error loading appointments: " + ex.getMessage(), StatusBanner.Type.ERROR);
        }
    }

    private void applyFilter() {
        tableModel.setRowCount(0);
        String selectedFilter = (String) statusFilterCombo.getSelectedItem();

        for (AppointmentDetails ad : allAppointments) {
            if (selectedFilter != null && !selectedFilter.equalsIgnoreCase("ALL")) {
                if (!selectedFilter.equalsIgnoreCase(ad.getStatus())) {
                    continue;
                }
            }

            tableModel.addRow(new Object[]{
                    "#" + ad.getAppointmentId(),
                    ad.getDoctorName() != null ? ad.getDoctorName() : "Doctor",
                    ad.getClinicDate() != null ? ad.getClinicDate().toString() : "-",
                    ad.getTimeRangeFormatted(),
                    ad.getReason() != null ? ad.getReason() : "",
                    ad.getStatus()
            });
        }

        if (tableModel.getRowCount() == 0) {
            statusBanner.showMessage("No appointment records found for status filter: " + selectedFilter, StatusBanner.Type.INFO);
        }
    }

    private void handleCancelAppointment() {
        statusBanner.clear();
        int row = table.getSelectedRow();
        if (row < 0) {
            statusBanner.showMessage("Please select an appointment from the table to cancel.", StatusBanner.Type.WARNING);
            return;
        }

        int apptId = Integer.parseInt(tableModel.getValueAt(row, 0).toString().replace("#", "").trim());
        String status = String.valueOf(tableModel.getValueAt(row, 5));
        String doctor = String.valueOf(tableModel.getValueAt(row, 1));
        String date = String.valueOf(tableModel.getValueAt(row, 2));

        if (!"BOOKED".equalsIgnoreCase(status)) {
            statusBanner.showMessage("Cannot cancel appointment with status '" + status + "'. Only BOOKED appointments can be cancelled.", StatusBanner.Type.ERROR);
            return;
        }

        boolean confirmed = ConfirmationDialog.showConfirm(
                SwingUtilities.getWindowAncestor(this),
                "Confirm Appointment Cancellation",
                "Are you sure you want to cancel Appointment <b>#" + apptId + "</b> with <b>" + doctor + "</b> on <b>" + date + "</b>?<br><br>" +
                        "This will call the database procedure to update status and free the slot for waitlisted patients.",
                "Cancel Appointment",
                true
        );

        if (confirmed) {
            try {
                DAOFactory.getInstance().getAppointmentDAO().cancelAppointment(apptId, currentPatient.getPatientId());
                statusBanner.showMessage("Appointment #" + apptId + " was cancelled successfully.", StatusBanner.Type.SUCCESS);
                loadAppointments();
            } catch (Exception ex) {
                statusBanner.showMessage("Cancellation failed: " + ex.getMessage(), StatusBanner.Type.ERROR);
            }
        }
    }
}
