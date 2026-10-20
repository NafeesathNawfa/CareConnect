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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Doctor/Volunteer schedule screen showing assigned appointments, patient consultations,
 * and outcome recording (COMPLETED / NO_SHOW) calling sp_set_appointment_outcome.
 */
public class DoctorScheduleScreen extends JPanel {

    private final ModernComboBox<DoctorItem> doctorCombo;
    private final ModernTextField dateField;
    private final StatusBanner statusBanner;
    private final ModernTable scheduleTable;
    private final DefaultTableModel tableModel;
    private List<AppointmentDetails> currentSchedule = new ArrayList<>();

    public static class DoctorItem {
        public final Doctor doctor;
        public DoctorItem(Doctor doctor) { this.doctor = doctor; }
        @Override
        public String toString() {
            if (doctor == null) return "-- Select Doctor --";
            return doctor.getFullName() + (doctor.isVolunteer() ? " [Volunteer]" : "") + " (" + doctor.getSpecializationName() + ")";
        }
    }

    public DoctorScheduleScreen() {
        setLayout(new BorderLayout());
        setBackground(UITheme.COLOR_BG_CANVAS);

        PageHeader header = new PageHeader("Doctor & Volunteer OPD Schedule", "View patient appointments and record consultation outcomes");
        ModernButton refreshBtn = new ModernButton("Refresh Schedule", ModernButton.StyleType.SECONDARY);
        refreshBtn.addActionListener(e -> loadSchedule());
        header.addAction(refreshBtn);
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(20, 28, 20, 28));

        statusBanner = new StatusBanner();
        center.add(statusBanner);
        center.add(Box.createVerticalStrut(14));

        // Filter Bar Card
        JPanel filterCard = new JPanel(new GridBagLayout());
        filterCard.setBackground(UITheme.COLOR_SURFACE);
        filterCard.setBorder(new CompoundBorder(
                new LineBorder(UITheme.COLOR_BORDER, 1, false),
                new EmptyBorder(16, 20, 16, 20)
        ));
        filterCard.setMaximumSize(new Dimension(1100, 100));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 8, 4, 8);

        doctorCombo = new ModernComboBox<>();
        dateField = new ModernTextField("YYYY-MM-DD (e.g. " + LocalDate.now() + ")");
        dateField.setText(LocalDate.now().toString());

        ModernButton searchBtn = new ModernButton("Load Schedule", ModernButton.StyleType.PRIMARY);
        searchBtn.addActionListener(e -> loadSchedule());

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.50;
        JLabel docLbl = new JLabel("Doctor / Volunteer:");
        docLbl.setFont(UITheme.FONT_BODY_BOLD);
        filterCard.add(docLbl, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.30;
        JLabel dateLbl = new JLabel("Date (YYYY-MM-DD):");
        dateLbl.setFont(UITheme.FONT_BODY_BOLD);
        filterCard.add(dateLbl, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.20;
        filterCard.add(new JLabel(""), gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        filterCard.add(doctorCombo, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        filterCard.add(dateField, gbc);

        gbc.gridx = 2; gbc.gridy = 1;
        filterCard.add(searchBtn, gbc);

        center.add(filterCard);
        center.add(Box.createVerticalStrut(18));

        // Schedule Table Section
        JPanel tableSection = new JPanel(new BorderLayout(0, 12));
        tableSection.setBackground(UITheme.COLOR_SURFACE);
        tableSection.setBorder(new CompoundBorder(
                new LineBorder(UITheme.COLOR_BORDER, 1, false),
                new EmptyBorder(18, 20, 18, 20)
        ));

        JLabel titleLbl = new JLabel("Scheduled Appointments & Patient Roster");
        titleLbl.setFont(UITheme.FONT_TITLE_MEDIUM);
        titleLbl.setForeground(UITheme.COLOR_DEEP_BLUE);
        tableSection.add(titleLbl, BorderLayout.NORTH);

        String[] cols = {"Appt ID", "Time Window", "Patient Name", "Phone", "Reason", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        scheduleTable = new ModernTable(tableModel);
        scheduleTable.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());
        scheduleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = ModernTable.wrapInScrollPane(scheduleTable);
        scroll.setPreferredSize(new Dimension(850, 320));
        tableSection.add(scroll, BorderLayout.CENTER);

        // Actions toolbar
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        actionRow.setOpaque(false);

        ModernButton completeBtn = new ModernButton("✓ Mark as COMPLETED", ModernButton.StyleType.PRIMARY);
        completeBtn.addActionListener(e -> handleOutcome("COMPLETED"));

        ModernButton noShowBtn = new ModernButton("Mark as NO-SHOW", ModernButton.StyleType.DANGER);
        noShowBtn.addActionListener(e -> handleOutcome("NO_SHOW"));

        actionRow.add(completeBtn);
        actionRow.add(noShowBtn);

        tableSection.add(actionRow, BorderLayout.SOUTH);

        center.add(tableSection);
        add(new JScrollPane(center), BorderLayout.CENTER);

        loadDoctors();
    }

    private void loadDoctors() {
        try {
            doctorCombo.removeAllItems();
            List<Doctor> docs = DAOFactory.getInstance().getDoctorDAO().getAllDoctors();
            for (Doctor d : docs) {
                doctorCombo.addItem(new DoctorItem(d));
            }
            if (doctorCombo.getItemCount() > 0) {
                doctorCombo.setSelectedIndex(0);
                loadSchedule();
            }
        } catch (Exception ex) {
            statusBanner.showMessage("Error loading doctors: " + ex.getMessage(), StatusBanner.Type.ERROR);
        }
    }

    public void loadSchedule() {
        statusBanner.clear();
        tableModel.setRowCount(0);

        DoctorItem item = (DoctorItem) doctorCombo.getSelectedItem();
        if (item == null || item.doctor == null) {
            statusBanner.showMessage("Please select a doctor to view their schedule.", StatusBanner.Type.WARNING);
            return;
        }

        LocalDate date = null;
        String dateStr = dateField.getText().trim();
        if (!dateStr.isEmpty()) {
            try {
                date = LocalDate.parse(dateStr);
            } catch (Exception ex) {
                statusBanner.showMessage("Invalid date format. Use YYYY-MM-DD or leave blank.", StatusBanner.Type.ERROR);
                return;
            }
        }

        try {
            currentSchedule = DAOFactory.getInstance().getAppointmentDAO().getScheduleByDoctor(item.doctor.getDoctorId(), date);
            for (AppointmentDetails ad : currentSchedule) {
                tableModel.addRow(new Object[]{
                        "#" + ad.getAppointmentId(),
                        ad.getTimeRangeFormatted(),
                        ad.getPatientName() != null ? ad.getPatientName() : "Unknown",
                        ad.getPatientPhone() != null ? ad.getPatientPhone() : "-",
                        ad.getReason() != null ? ad.getReason() : "",
                        ad.getStatus()
                });
            }

            if (currentSchedule.isEmpty()) {
                statusBanner.showMessage("No scheduled appointments found for " + item.doctor.getFullName() + (date != null ? " on " + date : "") + ".", StatusBanner.Type.INFO);
            }
        } catch (Exception ex) {
            statusBanner.showMessage("Error retrieving doctor schedule: " + ex.getMessage(), StatusBanner.Type.ERROR);
        }
    }

    private void handleOutcome(String outcome) {
        statusBanner.clear();
        int row = scheduleTable.getSelectedRow();
        if (row < 0) {
            statusBanner.showMessage("Please select an appointment from the table to record its outcome.", StatusBanner.Type.WARNING);
            return;
        }

        int apptId = Integer.parseInt(tableModel.getValueAt(row, 0).toString().replace("#", "").trim());
        String currentStatus = String.valueOf(tableModel.getValueAt(row, 5));
        String patient = String.valueOf(tableModel.getValueAt(row, 2));

        if (!"BOOKED".equalsIgnoreCase(currentStatus)) {
            statusBanner.showMessage("Outcome cannot be changed because appointment is already '" + currentStatus + "'.", StatusBanner.Type.ERROR);
            return;
        }

        boolean confirmed = ConfirmationDialog.showConfirm(
                SwingUtilities.getWindowAncestor(this),
                "Record Consultation Outcome",
                "Are you sure you want to mark Appointment <b>#" + apptId + "</b> for patient <b>" + patient + "</b> as <b>" + outcome + "</b>?<br><br>" +
                        "This calls sp_set_appointment_outcome in the database.",
                "Confirm Outcome",
                outcome.equals("NO_SHOW")
        );

        if (confirmed) {
            try {
                DAOFactory.getInstance().getAppointmentDAO().setAppointmentOutcome(apptId, outcome);
                statusBanner.showMessage("Appointment #" + apptId + " marked as " + outcome + " successfully.", StatusBanner.Type.SUCCESS);
                loadSchedule();
            } catch (Exception ex) {
                statusBanner.showMessage("Failed to update appointment outcome: " + ex.getMessage(), StatusBanner.Type.ERROR);
            }
        }
    }
}
