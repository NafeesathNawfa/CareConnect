package com.careconnect.ui.screens;

import com.careconnect.dao.DAOFactory;
import com.careconnect.model.AppointmentDetails;
import com.careconnect.model.Doctor;
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
 * Staff and Admin Master Appointment Registry with multi-criteria filtering,
 * search, cancellation by staff, and outcome recording.
 */
public class AdminAppointmentsScreen extends JPanel {

    private final ModernComboBox<String> statusFilterCombo;
    private final ModernComboBox<DoctorItem> doctorFilterCombo;
    private final ModernTextField searchField;
    private final StatusBanner statusBanner;
    private final ModernTable table;
    private final DefaultTableModel tableModel;
    private final JLabel recordCountLabel;

    public static class DoctorItem {
        public final Doctor doctor;
        public DoctorItem(Doctor doctor) { this.doctor = doctor; }
        @Override
        public String toString() {
            if (doctor == null) return "All Doctors";
            return doctor.getFullName();
        }
    }

    public AdminAppointmentsScreen() {
        setLayout(new BorderLayout());
        setBackground(UITheme.COLOR_BG_CANVAS);

        PageHeader header = new PageHeader("Master Appointment Registry", "Comprehensive clinic appointment records and status management");
        ModernButton refreshBtn = new ModernButton("Refresh All", ModernButton.StyleType.SECONDARY);
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

        // Filter Card
        JPanel filterCard = new JPanel(new GridBagLayout());
        filterCard.setBackground(UITheme.COLOR_SURFACE);
        filterCard.setBorder(new CompoundBorder(
                new LineBorder(UITheme.COLOR_BORDER, 1, false),
                new EmptyBorder(16, 20, 16, 20)
        ));
        filterCard.setMaximumSize(new Dimension(1100, 110));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 6, 4, 6);

        statusFilterCombo = new ModernComboBox<>(new String[]{"ALL", "BOOKED", "COMPLETED", "CANCELLED", "NO_SHOW"});
        doctorFilterCombo = new ModernComboBox<>();
        searchField = new ModernTextField("Search patient name or phone...");

        ModernButton filterBtn = new ModernButton("Search", ModernButton.StyleType.PRIMARY);
        filterBtn.addActionListener(e -> loadAppointments());

        ModernButton resetBtn = new ModernButton("Reset", ModernButton.StyleType.SECONDARY);
        resetBtn.addActionListener(e -> {
            statusFilterCombo.setSelectedIndex(0);
            doctorFilterCombo.setSelectedIndex(0);
            searchField.setText("");
            loadAppointments();
        });

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.25;
        JLabel sLbl = new JLabel("Status:");
        sLbl.setFont(UITheme.FONT_BODY_BOLD);
        filterCard.add(sLbl, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.30;
        JLabel dLbl = new JLabel("Doctor:");
        dLbl.setFont(UITheme.FONT_BODY_BOLD);
        filterCard.add(dLbl, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.35;
        JLabel qLbl = new JLabel("Patient Search:");
        qLbl.setFont(UITheme.FONT_BODY_BOLD);
        filterCard.add(qLbl, gbc);

        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 0.10;
        filterCard.add(new JLabel(""), gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        filterCard.add(statusFilterCombo, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        filterCard.add(doctorFilterCombo, gbc);

        gbc.gridx = 2; gbc.gridy = 1;
        filterCard.add(searchField, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(filterBtn);
        btnPanel.add(resetBtn);
        gbc.gridx = 3; gbc.gridy = 1;
        filterCard.add(btnPanel, gbc);

        center.add(filterCard);
        center.add(Box.createVerticalStrut(18));

        // Registry Table
        JPanel tableSection = new JPanel(new BorderLayout(0, 12));
        tableSection.setBackground(UITheme.COLOR_SURFACE);
        tableSection.setBorder(new CompoundBorder(
                new LineBorder(UITheme.COLOR_BORDER, 1, false),
                new EmptyBorder(18, 20, 18, 20)
        ));

        JPanel sectionHeader = new JPanel(new BorderLayout());
        sectionHeader.setOpaque(false);

        JLabel sectionTitle = new JLabel("All Appointment Records (v_appointment_details)");
        sectionTitle.setFont(UITheme.FONT_TITLE_MEDIUM);
        sectionTitle.setForeground(UITheme.COLOR_DEEP_BLUE);

        recordCountLabel = new JLabel("0 records");
        recordCountLabel.setFont(UITheme.FONT_SMALL);
        recordCountLabel.setForeground(UITheme.COLOR_TEXT_MUTED);

        sectionHeader.add(sectionTitle, BorderLayout.WEST);
        sectionHeader.add(recordCountLabel, BorderLayout.EAST);
        tableSection.add(sectionHeader, BorderLayout.NORTH);

        String[] cols = {"Appt ID", "Patient Name", "Phone", "Doctor", "Date", "Time", "Reason", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new ModernTable(tableModel);
        table.getColumnModel().getColumn(7).setCellRenderer(new StatusBadgeRenderer());
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = ModernTable.wrapInScrollPane(table);
        scroll.setPreferredSize(new Dimension(850, 340));
        tableSection.add(scroll, BorderLayout.CENTER);

        // Staff Action Bar
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        actionRow.setOpaque(false);

        ModernButton cancelStaffBtn = new ModernButton("Staff Cancel Appt", ModernButton.StyleType.DANGER);
        cancelStaffBtn.addActionListener(e -> handleStaffCancel());

        ModernButton markCompBtn = new ModernButton("Mark Completed", ModernButton.StyleType.PRIMARY);
        markCompBtn.addActionListener(e -> handleSetOutcome("COMPLETED"));

        ModernButton markNoShowBtn = new ModernButton("Mark No-Show", ModernButton.StyleType.SECONDARY);
        markNoShowBtn.addActionListener(e -> handleSetOutcome("NO_SHOW"));

        actionRow.add(cancelStaffBtn);
        actionRow.add(markNoShowBtn);
        actionRow.add(markCompBtn);

        tableSection.add(actionRow, BorderLayout.SOUTH);

        center.add(tableSection);
        add(new JScrollPane(center), BorderLayout.CENTER);

        loadDoctorFilter();
        loadAppointments();
    }

    private void loadDoctorFilter() {
        try {
            doctorFilterCombo.removeAllItems();
            doctorFilterCombo.addItem(new DoctorItem(null));
            List<Doctor> docs = DAOFactory.getInstance().getDoctorDAO().getAllDoctors();
            for (Doctor d : docs) {
                doctorFilterCombo.addItem(new DoctorItem(d));
            }
        } catch (Exception ex) {
            statusBanner.showMessage("Error loading doctors: " + ex.getMessage(), StatusBanner.Type.ERROR);
        }
    }

    public void loadAppointments() {
        statusBanner.clear();
        tableModel.setRowCount(0);

        String status = (String) statusFilterCombo.getSelectedItem();
        DoctorItem docItem = (DoctorItem) doctorFilterCombo.getSelectedItem();
        Integer docId = (docItem != null && docItem.doctor != null) ? docItem.doctor.getDoctorId() : null;
        String search = searchField.getText().trim();

        try {
            List<AppointmentDetails> list = DAOFactory.getInstance().getAppointmentDAO().getAllAppointments(status, docId, search);
            recordCountLabel.setText(list.size() + " appointment record(s) found");

            for (AppointmentDetails ad : list) {
                tableModel.addRow(new Object[]{
                        "#" + ad.getAppointmentId(),
                        ad.getPatientName() != null ? ad.getPatientName() : "Patient #" + ad.getPatientId(),
                        ad.getPatientPhone() != null ? ad.getPatientPhone() : "-",
                        ad.getDoctorName() != null ? ad.getDoctorName() : "Doctor #" + ad.getDoctorId(),
                        ad.getClinicDate() != null ? ad.getClinicDate().toString() : "-",
                        ad.getTimeRangeFormatted(),
                        ad.getReason() != null ? ad.getReason() : "",
                        ad.getStatus()
                });
            }

            if (list.isEmpty()) {
                statusBanner.showMessage("No appointment records matched the specified filter criteria.", StatusBanner.Type.INFO);
            }
        } catch (Exception ex) {
            statusBanner.showMessage("Error retrieving appointments: " + ex.getMessage(), StatusBanner.Type.ERROR);
        }
    }

    private void handleStaffCancel() {
        statusBanner.clear();
        int row = table.getSelectedRow();
        if (row < 0) {
            statusBanner.showMessage("Please select an appointment from the table to cancel.", StatusBanner.Type.WARNING);
            return;
        }

        int apptId = Integer.parseInt(tableModel.getValueAt(row, 0).toString().replace("#", "").trim());
        String status = String.valueOf(tableModel.getValueAt(row, 7));
        String patient = String.valueOf(tableModel.getValueAt(row, 1));
        String doc = String.valueOf(tableModel.getValueAt(row, 3));

        if (!"BOOKED".equalsIgnoreCase(status)) {
            statusBanner.showMessage("Only BOOKED appointments can be cancelled (current status: " + status + ").", StatusBanner.Type.ERROR);
            return;
        }

        boolean confirmed = ConfirmationDialog.showConfirm(
                SwingUtilities.getWindowAncestor(this),
                "Staff Cancellation Override",
                "As clinic staff, do you confirm cancellation of Appointment <b>#" + apptId + "</b> for patient <b>" + patient + "</b> with <b>" + doc + "</b>?<br><br>" +
                        "This calls sp_cancel_appointment with patient_id = NULL to authorize staff cancellation.",
                "Cancel Appointment",
                true
        );

        if (confirmed) {
            try {
                // Pass null patient_id for staff cancellation per sp_cancel_appointment specification
                DAOFactory.getInstance().getAppointmentDAO().cancelAppointment(apptId, null);
                statusBanner.showMessage("Appointment #" + apptId + " was cancelled by staff. Slot has been freed.", StatusBanner.Type.SUCCESS);
                loadAppointments();
            } catch (Exception ex) {
                statusBanner.showMessage("Cancellation failed: " + ex.getMessage(), StatusBanner.Type.ERROR);
            }
        }
    }

    private void handleSetOutcome(String outcome) {
        statusBanner.clear();
        int row = table.getSelectedRow();
        if (row < 0) {
            statusBanner.showMessage("Please select an appointment from the table to update outcome.", StatusBanner.Type.WARNING);
            return;
        }

        int apptId = Integer.parseInt(tableModel.getValueAt(row, 0).toString().replace("#", "").trim());
        String status = String.valueOf(tableModel.getValueAt(row, 7));

        if (!"BOOKED".equalsIgnoreCase(status)) {
            statusBanner.showMessage("Outcome cannot be changed because appointment is already '" + status + "'.", StatusBanner.Type.ERROR);
            return;
        }

        try {
            DAOFactory.getInstance().getAppointmentDAO().setAppointmentOutcome(apptId, outcome);
            statusBanner.showMessage("Appointment #" + apptId + " marked as " + outcome + ".", StatusBanner.Type.SUCCESS);
            loadAppointments();
        } catch (Exception ex) {
            statusBanner.showMessage("Failed to update status: " + ex.getMessage(), StatusBanner.Type.ERROR);
        }
    }
}
