package com.careconnect.ui.screens;

import com.careconnect.dao.DAOFactory;
import com.careconnect.model.*;
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
import java.time.LocalTime;
import java.util.List;

/**
 * Unified administrative management screen for Doctors, Clinic Days, Slot Generation, and Waitlists.
 */
public class AdminManagementScreen extends JPanel {

    private final JTabbedPane tabbedPane;
    private final StatusBanner statusBanner;

    // Doctor tab components
    private DefaultTableModel doctorTableModel;
    private ModernTable doctorTable;

    // Clinic Day tab components
    private DefaultTableModel clinicDayTableModel;
    private ModernTable clinicDayTable;
    private ModernTextField newDateDayField;
    private JCheckBox newDayOpenCheck;
    private ModernTextField newDayNotesField;

    // Slot tab components
    private ModernComboBox<Doctor> slotDoctorCombo;
    private ModernComboBox<ClinicDay> slotDayCombo;
    private ModernTextField slotStartTimeField;
    private ModernTextField slotEndTimeField;
    private ModernTextField slotMinutesField;
    private DefaultTableModel slotTableModel;
    private ModernTable slotTable;

    // Waitlist tab components
    private DefaultTableModel waitlistTableModel;
    private ModernTable waitlistTable;

    public AdminManagementScreen(String initialTab) {
        setLayout(new BorderLayout());
        setBackground(UITheme.COLOR_BG_CANVAS);

        PageHeader header = new PageHeader("Clinic Administration & Configuration", "Manage medical staff, operational calendar, appointment slots, and patient waitlists");
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(16, 24, 20, 24));

        statusBanner = new StatusBanner();
        center.add(statusBanner, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BODY_BOLD);
        tabbedPane.setBackground(UITheme.COLOR_SURFACE);
        tabbedPane.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        tabbedPane.addTab("Doctor Management", createDoctorPanel());
        tabbedPane.addTab("Clinic Days Calendar", createClinicDayPanel());
        tabbedPane.addTab("Slot Generator & Management", createSlotPanel());
        tabbedPane.addTab("Waitlist Queue", createWaitlistPanel());

        if ("manage_days".equals(initialTab)) {
            tabbedPane.setSelectedIndex(1);
        } else if ("manage_waitlist".equals(initialTab)) {
            tabbedPane.setSelectedIndex(3);
        }

        center.add(tabbedPane, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    public void selectTab(String key) {
        if ("manage_doctors".equals(key)) tabbedPane.setSelectedIndex(0);
        else if ("manage_days".equals(key)) tabbedPane.setSelectedIndex(1);
        else if ("manage_waitlist".equals(key)) tabbedPane.setSelectedIndex(3);
    }

    // ==========================================
    // TAB 1: DOCTOR MANAGEMENT
    // ==========================================
    private JPanel createDoctorPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(UITheme.COLOR_SURFACE);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        JLabel title = new JLabel("Registered Doctors and Volunteer Physicians");
        title.setFont(UITheme.FONT_TITLE_MEDIUM);
        title.setForeground(UITheme.COLOR_DEEP_BLUE);

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnGroup.setOpaque(false);
        ModernButton addDocBtn = new ModernButton("+ Add Doctor", ModernButton.StyleType.PRIMARY);
        addDocBtn.addActionListener(e -> showAddDoctorDialog());
        ModernButton refreshBtn = new ModernButton("Refresh", ModernButton.StyleType.SECONDARY);
        refreshBtn.addActionListener(e -> loadDoctors());
        btnGroup.add(addDocBtn);
        btnGroup.add(refreshBtn);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(btnGroup, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        String[] cols = {"ID", "Full Name", "Specialization", "Phone", "Email", "Volunteer?", "Active Status"};
        doctorTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        doctorTable = new ModernTable(doctorTableModel);
        doctorTable.getColumnModel().getColumn(6).setCellRenderer(new StatusBadgeRenderer());

        JScrollPane scroll = ModernTable.wrapInScrollPane(doctorTable);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomBar.setOpaque(false);
        ModernButton toggleActiveBtn = new ModernButton("Toggle Active/Inactive", ModernButton.StyleType.SECONDARY);
        toggleActiveBtn.addActionListener(e -> handleToggleDoctorActive());
        bottomBar.add(toggleActiveBtn);
        panel.add(bottomBar, BorderLayout.SOUTH);

        loadDoctors();
        return panel;
    }

    private void loadDoctors() {
        statusBanner.clear();
        doctorTableModel.setRowCount(0);
        try {
            List<Doctor> docs = DAOFactory.getInstance().getDoctorDAO().getAllDoctors();
            for (Doctor d : docs) {
                doctorTableModel.addRow(new Object[]{
                        d.getDoctorId(),
                        d.getFullName(),
                        d.getSpecializationName() != null ? d.getSpecializationName() : "General",
                        d.getPhone(),
                        d.getEmail() != null ? d.getEmail() : "-",
                        d.isVolunteer() ? "Volunteer" : "Staff Physician",
                        d.isActive() ? "ACTIVE" : "BLOCKED"
                });
            }
        } catch (Exception ex) {
            statusBanner.showMessage("Error loading doctors: " + ex.getMessage(), StatusBanner.Type.ERROR);
        }
    }

    private void showAddDoctorDialog() {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Add New Doctor", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.getContentPane().setBackground(UITheme.COLOR_SURFACE);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 6, 6, 6);

        ModernTextField nameFld = new ModernTextField("Full Name (e.g. Dr. Rajesh Sen)");
        ModernComboBox<Specialization> specCombo = new ModernComboBox<>();
        ModernTextField phoneFld = new ModernTextField("Phone Number");
        ModernTextField emailFld = new ModernTextField("Email Address");
        JCheckBox volunteerChk = new JCheckBox("Is Volunteer Physician", true);
        volunteerChk.setFont(UITheme.FONT_BODY);
        volunteerChk.setOpaque(false);

        try {
            List<Specialization> specs = DAOFactory.getInstance().getDoctorDAO().getAllSpecializations();
            for (Specialization s : specs) specCombo.addItem(s);
        } catch (Exception ignored) {}

        int r = 0;
        g.gridx = 0; g.gridy = r; form.add(new JLabel("Full Name:"), g);
        g.gridx = 1; g.gridy = r++; form.add(nameFld, g);
        g.gridx = 0; g.gridy = r; form.add(new JLabel("Specialization:"), g);
        g.gridx = 1; g.gridy = r++; form.add(specCombo, g);
        g.gridx = 0; g.gridy = r; form.add(new JLabel("Phone:"), g);
        g.gridx = 1; g.gridy = r++; form.add(phoneFld, g);
        g.gridx = 0; g.gridy = r; form.add(new JLabel("Email:"), g);
        g.gridx = 1; g.gridy = r++; form.add(emailFld, g);
        g.gridx = 1; g.gridy = r++; form.add(volunteerChk, g);

        dlg.add(form, BorderLayout.CENTER);

        JPanel btnPnl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnPnl.setBackground(UITheme.COLOR_BG_CANVAS);
        ModernButton cancelBtn = new ModernButton("Cancel", ModernButton.StyleType.SECONDARY);
        cancelBtn.addActionListener(ev -> dlg.dispose());
        ModernButton saveBtn = new ModernButton("Save Doctor", ModernButton.StyleType.PRIMARY);
        saveBtn.addActionListener(ev -> {
            String name = nameFld.getText().trim();
            String phone = phoneFld.getText().trim();
            if (name.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Name and Phone are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Specialization selSpec = (Specialization) specCombo.getSelectedItem();
            int specId = selSpec != null ? selSpec.getSpecializationId() : 1;
            Doctor doc = new Doctor(0, name, specId, phone, emailFld.getText().trim(), volunteerChk.isSelected(), true);
            try {
                DAOFactory.getInstance().getDoctorDAO().addDoctor(doc);
                dlg.dispose();
                loadDoctors();
                statusBanner.showMessage("Doctor " + name + " added successfully.", StatusBanner.Type.SUCCESS);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Error adding doctor: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPnl.add(cancelBtn);
        btnPnl.add(saveBtn);
        dlg.add(btnPnl, BorderLayout.SOUTH);

        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void handleToggleDoctorActive() {
        int r = doctorTable.getSelectedRow();
        if (r < 0) {
            statusBanner.showMessage("Select a doctor to toggle active status.", StatusBanner.Type.WARNING);
            return;
        }
        int docId = (int) doctorTableModel.getValueAt(r, 0);
        String name = (String) doctorTableModel.getValueAt(r, 1);
        String currentStatus = (String) doctorTableModel.getValueAt(r, 6);
        boolean newStatus = !"ACTIVE".equalsIgnoreCase(currentStatus);

        try {
            DAOFactory.getInstance().getDoctorDAO().toggleDoctorActive(docId, newStatus);
            statusBanner.showMessage("Doctor " + name + " is now " + (newStatus ? "Active" : "Inactive") + ".", StatusBanner.Type.SUCCESS);
            loadDoctors();
        } catch (Exception ex) {
            statusBanner.showMessage("Failed to update doctor: " + ex.getMessage(), StatusBanner.Type.ERROR);
        }
    }

    // ==========================================
    // TAB 2: CLINIC DAYS CALENDAR
    // ==========================================
    private JPanel createClinicDayPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(UITheme.COLOR_SURFACE);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Form at top: configure clinic day (sp_set_clinic_day)
        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(UITheme.COLOR_BG_CANVAS);
        formCard.setBorder(new CompoundBorder(new LineBorder(UITheme.COLOR_BORDER, 1), new EmptyBorder(12, 16, 12, 16)));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 8, 4, 8);

        newDateDayField = new ModernTextField("YYYY-MM-DD (e.g. " + LocalDate.now().plusDays(4) + ")");
        newDayOpenCheck = new JCheckBox("Clinic Is Open", true);
        newDayOpenCheck.setFont(UITheme.FONT_BODY_BOLD);
        newDayOpenCheck.setOpaque(false);
        newDayNotesField = new ModernTextField("Notes (e.g. Regular OPD, Specialty Clinic, Holiday)");

        ModernButton saveDayBtn = new ModernButton("Save Clinic Day (sp_set_clinic_day)", ModernButton.StyleType.PRIMARY);
        saveDayBtn.addActionListener(e -> handleSaveClinicDay());

        g.gridx = 0; g.gridy = 0; formCard.add(new JLabel("Clinic Date:"), g);
        g.gridx = 1; g.gridy = 0; formCard.add(newDateDayField, g);
        g.gridx = 2; g.gridy = 0; formCard.add(newDayOpenCheck, g);
        g.gridx = 0; g.gridy = 1; formCard.add(new JLabel("Notes:"), g);
        g.gridx = 1; g.gridy = 1; formCard.add(newDayNotesField, g);
        g.gridx = 2; g.gridy = 1; formCard.add(saveDayBtn, g);

        panel.add(formCard, BorderLayout.NORTH);

        // Table of days
        String[] cols = {"Day ID", "Date", "Status", "Notes / Description"};
        clinicDayTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        clinicDayTable = new ModernTable(clinicDayTableModel);
        clinicDayTable.getColumnModel().getColumn(2).setCellRenderer(new StatusBadgeRenderer());

        JScrollPane scroll = ModernTable.wrapInScrollPane(clinicDayTable);
        panel.add(scroll, BorderLayout.CENTER);

        loadClinicDays();
        return panel;
    }

    private void loadClinicDays() {
        clinicDayTableModel.setRowCount(0);
        try {
            List<ClinicDay> days = DAOFactory.getInstance().getClinicDayDAO().getAllClinicDays();
            for (ClinicDay cd : days) {
                clinicDayTableModel.addRow(new Object[]{
                        cd.getClinicDayId(),
                        cd.getClinicDate().toString(),
                        cd.isOpen() ? "OPEN" : "CLOSED",
                        cd.getNotes() != null ? cd.getNotes() : ""
                });
            }
        } catch (Exception ex) {
            statusBanner.showMessage("Error loading clinic days: " + ex.getMessage(), StatusBanner.Type.ERROR);
        }
    }

    private void handleSaveClinicDay() {
        statusBanner.clear();
        String dateStr = newDateDayField.getText().trim();
        if (dateStr.isEmpty()) {
            statusBanner.showMessage("Date is required.", StatusBanner.Type.WARNING);
            return;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (Exception ex) {
            statusBanner.showMessage("Invalid date format. Use YYYY-MM-DD.", StatusBanner.Type.ERROR);
            return;
        }

        boolean isOpen = newDayOpenCheck.isSelected();
        String notes = newDayNotesField.getText().trim();

        try {
            DAOFactory.getInstance().getClinicDayDAO().setClinicDay(date, isOpen, notes);
            statusBanner.showMessage("Clinic day for " + date + " updated successfully (sp_set_clinic_day).", StatusBanner.Type.SUCCESS);
            loadClinicDays();
            newDateDayField.setText("");
            newDayNotesField.setText("");
        } catch (Exception ex) {
            statusBanner.showMessage("Failed to set clinic day: " + ex.getMessage(), StatusBanner.Type.ERROR);
        }
    }

    // ==========================================
    // TAB 3: SLOT GENERATOR & MANAGEMENT
    // ==========================================
    private JPanel createSlotPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(UITheme.COLOR_SURFACE);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Generator Card (sp_generate_slots)
        JPanel genCard = new JPanel(new GridBagLayout());
        genCard.setBackground(UITheme.COLOR_BG_CANVAS);
        genCard.setBorder(new CompoundBorder(new LineBorder(UITheme.COLOR_BORDER, 1), new EmptyBorder(12, 16, 12, 16)));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 6, 4, 6);

        slotDoctorCombo = new ModernComboBox<>();
        slotDayCombo = new ModernComboBox<>();
        slotStartTimeField = new ModernTextField("09:00");
        slotEndTimeField = new ModernTextField("12:00");
        slotMinutesField = new ModernTextField("20");

        ModernButton genBtn = new ModernButton("Generate Slots (sp_generate_slots)", ModernButton.StyleType.PRIMARY);
        genBtn.addActionListener(e -> handleGenerateSlots());

        ModernButton loadSlotsBtn = new ModernButton("View Doctor Slots", ModernButton.StyleType.SECONDARY);
        loadSlotsBtn.addActionListener(e -> loadSlots());

        g.gridx = 0; g.gridy = 0; genCard.add(new JLabel("Doctor:"), g);
        g.gridx = 1; g.gridy = 0; genCard.add(slotDoctorCombo, g);
        g.gridx = 2; g.gridy = 0; genCard.add(new JLabel("Clinic Day:"), g);
        g.gridx = 3; g.gridy = 0; genCard.add(slotDayCombo, g);

        g.gridx = 0; g.gridy = 1; genCard.add(new JLabel("Start Time (HH:MM):"), g);
        g.gridx = 1; g.gridy = 1; genCard.add(slotStartTimeField, g);
        g.gridx = 2; g.gridy = 1; genCard.add(new JLabel("End Time (HH:MM):"), g);
        g.gridx = 3; g.gridy = 1; genCard.add(slotEndTimeField, g);

        g.gridx = 0; g.gridy = 2; genCard.add(new JLabel("Slot Mins:"), g);
        g.gridx = 1; g.gridy = 2; genCard.add(slotMinutesField, g);
        g.gridx = 2; g.gridy = 2; genCard.add(loadSlotsBtn, g);
        g.gridx = 3; g.gridy = 2; genCard.add(genBtn, g);

        panel.add(genCard, BorderLayout.NORTH);

        // Slots Table
        String[] cols = {"Slot ID", "Time Window", "Status"};
        slotTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        slotTable = new ModernTable(slotTableModel);
        slotTable.getColumnModel().getColumn(2).setCellRenderer(new StatusBadgeRenderer());

        JScrollPane scroll = ModernTable.wrapInScrollPane(slotTable);
        panel.add(scroll, BorderLayout.CENTER);

        // Bottom Bar (sp_block_slot)
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomBar.setOpaque(false);
        ModernButton blockBtn = new ModernButton("Block Slot", ModernButton.StyleType.DANGER);
        blockBtn.addActionListener(e -> handleBlockSlot(true));
        ModernButton unblockBtn = new ModernButton("Unblock Slot", ModernButton.StyleType.SECONDARY);
        unblockBtn.addActionListener(e -> handleBlockSlot(false));
        bottomBar.add(unblockBtn);
        bottomBar.add(blockBtn);
        panel.add(bottomBar, BorderLayout.SOUTH);

        loadSlotGeneratorCombos();
        return panel;
    }

    private void loadSlotGeneratorCombos() {
        try {
            slotDoctorCombo.removeAllItems();
            List<Doctor> docs = DAOFactory.getInstance().getDoctorDAO().getActiveDoctors();
            for (Doctor d : docs) slotDoctorCombo.addItem(d);

            slotDayCombo.removeAllItems();
            List<ClinicDay> days = DAOFactory.getInstance().getClinicDayDAO().getAllClinicDays();
            for (ClinicDay cd : days) slotDayCombo.addItem(cd);
        } catch (Exception ignored) {}
    }

    private void handleGenerateSlots() {
        statusBanner.clear();
        Doctor doc = (Doctor) slotDoctorCombo.getSelectedItem();
        ClinicDay day = (ClinicDay) slotDayCombo.getSelectedItem();

        if (doc == null || day == null) {
            statusBanner.showMessage("Select a doctor and clinic day.", StatusBanner.Type.WARNING);
            return;
        }

        try {
            LocalTime start = LocalTime.parse(slotStartTimeField.getText().trim());
            LocalTime end = LocalTime.parse(slotEndTimeField.getText().trim());
            int mins = Integer.parseInt(slotMinutesField.getText().trim());

            DAOFactory.getInstance().getSlotDAO().generateSlots(doc.getDoctorId(), day.getClinicDayId(), start, end, mins);
            statusBanner.showMessage("Slots successfully generated for " + doc.getFullName() + " on " + day.getClinicDate() + ".", StatusBanner.Type.SUCCESS);
            loadSlots();
        } catch (Exception ex) {
            statusBanner.showMessage("Error generating slots: " + ex.getMessage(), StatusBanner.Type.ERROR);
        }
    }

    private void loadSlots() {
        slotTableModel.setRowCount(0);
        Doctor doc = (Doctor) slotDoctorCombo.getSelectedItem();
        ClinicDay day = (ClinicDay) slotDayCombo.getSelectedItem();
        if (doc == null || day == null) return;

        try {
            List<Slot> slots = DAOFactory.getInstance().getSlotDAO().getSlotsByDoctorAndDay(doc.getDoctorId(), day.getClinicDayId());
            for (Slot s : slots) {
                slotTableModel.addRow(new Object[]{
                        "#" + s.getSlotId(),
                        s.getTimeRangeFormatted(),
                        s.getStatus()
                });
            }
            if (slots.isEmpty()) {
                statusBanner.showMessage("No slots exist yet for " + doc.getFullName() + " on this date. Click 'Generate Slots' to create them.", StatusBanner.Type.INFO);
            }
        } catch (Exception ex) {
            statusBanner.showMessage("Error loading slots: " + ex.getMessage(), StatusBanner.Type.ERROR);
        }
    }

    private void handleBlockSlot(boolean block) {
        statusBanner.clear();
        int r = slotTable.getSelectedRow();
        if (r < 0) {
            statusBanner.showMessage("Select a slot in the table first.", StatusBanner.Type.WARNING);
            return;
        }

        int slotId = Integer.parseInt(slotTableModel.getValueAt(r, 0).toString().replace("#", "").trim());
        try {
            DAOFactory.getInstance().getSlotDAO().blockSlot(slotId, block);
            statusBanner.showMessage("Slot #" + slotId + (block ? " blocked" : " unblocked") + " successfully (sp_block_slot).", StatusBanner.Type.SUCCESS);
            loadSlots();
        } catch (Exception ex) {
            statusBanner.showMessage("Failed to update slot: " + ex.getMessage(), StatusBanner.Type.ERROR);
        }
    }

    // ==========================================
    // TAB 4: WAITLIST QUEUE
    // ==========================================
    private JPanel createWaitlistPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(UITheme.COLOR_SURFACE);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        JLabel title = new JLabel("Active Waitlist Queue (v_waitlist_queue)");
        title.setFont(UITheme.FONT_TITLE_MEDIUM);
        title.setForeground(UITheme.COLOR_DEEP_BLUE);

        ModernButton refreshBtn = new ModernButton("Refresh Queue", ModernButton.StyleType.SECONDARY);
        refreshBtn.addActionListener(e -> loadWaitlist());
        topBar.add(title, BorderLayout.WEST);
        topBar.add(refreshBtn, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        String[] cols = {"Waitlist ID", "Patient Name", "Doctor", "Clinic Date", "Queue Position", "Status"};
        waitlistTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        waitlistTable = new ModernTable(waitlistTableModel);
        waitlistTable.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());

        JScrollPane scroll = ModernTable.wrapInScrollPane(waitlistTable);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomBar.setOpaque(false);
        ModernButton removeBtn = new ModernButton("Remove from Waitlist", ModernButton.StyleType.DANGER);
        removeBtn.addActionListener(e -> handleRemoveWaitlist());
        bottomBar.add(removeBtn);
        panel.add(bottomBar, BorderLayout.SOUTH);

        loadWaitlist();
        return panel;
    }

    private void loadWaitlist() {
        waitlistTableModel.setRowCount(0);
        try {
            List<WaitlistEntry> list = DAOFactory.getInstance().getWaitlistDAO().getWaitlistQueue(null, null);
            for (WaitlistEntry w : list) {
                waitlistTableModel.addRow(new Object[]{
                        "#" + w.getWaitlistId(),
                        w.getPatientName(),
                        w.getDoctorName(),
                        w.getClinicDate() != null ? w.getClinicDate().toString() : "-",
                        "Position #" + w.getPositionInQueue(),
                        w.getStatus()
                });
            }
        } catch (Exception ex) {
            statusBanner.showMessage("Error loading waitlist: " + ex.getMessage(), StatusBanner.Type.ERROR);
        }
    }

    private void handleRemoveWaitlist() {
        statusBanner.clear();
        int r = waitlistTable.getSelectedRow();
        if (r < 0) {
            statusBanner.showMessage("Select a waitlist entry to remove.", StatusBanner.Type.WARNING);
            return;
        }

        int wid = Integer.parseInt(waitlistTableModel.getValueAt(r, 0).toString().replace("#", "").trim());
        String name = String.valueOf(waitlistTableModel.getValueAt(r, 1));

        boolean confirmed = ConfirmationDialog.showConfirm(
                SwingUtilities.getWindowAncestor(this),
                "Remove Patient from Waitlist",
                "Remove patient <b>" + name + "</b> from the waitlist?<br><br>They will lose their place in the queue.",
                "Remove Patient",
                true
        );

        if (confirmed) {
            try {
                DAOFactory.getInstance().getWaitlistDAO().leaveWaitlist(wid, null);
                statusBanner.showMessage("Waitlist entry #" + wid + " removed.", StatusBanner.Type.SUCCESS);
                loadWaitlist();
            } catch (Exception ex) {
                statusBanner.showMessage("Error: " + ex.getMessage(), StatusBanner.Type.ERROR);
            }
        }
    }
}
