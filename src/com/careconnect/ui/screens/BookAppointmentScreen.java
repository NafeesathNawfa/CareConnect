package com.careconnect.ui.screens;

import com.careconnect.dao.DAOFactory;
import com.careconnect.model.ClinicDay;
import com.careconnect.model.Doctor;
import com.careconnect.model.Patient;
import com.careconnect.model.Slot;
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
 * Screen enabling patients to search available slots, book an appointment atomically,
 * or join the clinic waitlist if all slots are booked.
 */
public class BookAppointmentScreen extends JPanel {

    public interface BookingSuccessCallback {
        void onAppointmentBooked();
    }

    private final Patient currentPatient;
    private final BookingSuccessCallback callback;

    private final ModernComboBox<DoctorItem> doctorCombo;
    private final ModernComboBox<ClinicDayItem> clinicDayCombo;
    private final ModernTextField reasonField;
    private final StatusBanner statusBanner;
    private final DefaultTableModel slotsTableModel;
    private final ModernTable slotsTable;
    private final ModernButton bookButton;
    private final ModernButton waitlistButton;
    private final JLabel slotCountLabel;

    private List<Slot> currentAvailableSlots = new ArrayList<>();

    public static class DoctorItem {
        public final Doctor doctor;
        public DoctorItem(Doctor doctor) { this.doctor = doctor; }
        @Override
        public String toString() {
            if (doctor == null) return "All Available Doctors";
            return doctor.getFullName() + " (" + (doctor.getSpecializationName() != null ? doctor.getSpecializationName() : "General") + ")";
        }
    }

    public static class ClinicDayItem {
        public final ClinicDay day;
        public ClinicDayItem(ClinicDay day) { this.day = day; }
        @Override
        public String toString() {
            if (day == null) return "All Upcoming Clinic Days";
            return day.getClinicDate().toString() + (day.isOpen() ? " [Open: " + (day.getNotes() != null ? day.getNotes() : "OPD") + "]" : " [Closed]");
        }
    }

    public BookAppointmentScreen(Patient patient, BookingSuccessCallback callback) {
        this.currentPatient = patient;
        this.callback = callback;
        setLayout(new BorderLayout());
        setBackground(UITheme.COLOR_BG_CANVAS);

        PageHeader header = new PageHeader("Schedule an Appointment", "Select a doctor and available clinic time slot");
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(20, 28, 20, 28));

        statusBanner = new StatusBanner();
        center.add(statusBanner);
        center.add(Box.createVerticalStrut(14));

        // Step 1: Filter Panel
        JPanel filterCard = new JPanel(new GridBagLayout());
        filterCard.setBackground(UITheme.COLOR_SURFACE);
        filterCard.setBorder(new CompoundBorder(
                new LineBorder(UITheme.COLOR_BORDER, 1, false),
                new EmptyBorder(16, 20, 16, 20)
        ));
        filterCard.setMaximumSize(new Dimension(1100, 120));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 8, 4, 8);

        doctorCombo = new ModernComboBox<>();
        clinicDayCombo = new ModernComboBox<>();

        ModernButton searchSlotsBtn = new ModernButton("Search Available Slots", ModernButton.StyleType.PRIMARY);
        searchSlotsBtn.addActionListener(e -> loadAvailableSlots());

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.45;
        JLabel docLbl = new JLabel("Doctor / Specialist:");
        docLbl.setFont(UITheme.FONT_BODY_BOLD);
        filterCard.add(docLbl, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.35;
        JLabel dayLbl = new JLabel("Clinic Day:");
        dayLbl.setFont(UITheme.FONT_BODY_BOLD);
        filterCard.add(dayLbl, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.20;
        filterCard.add(new JLabel(""), gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        filterCard.add(doctorCombo, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        filterCard.add(clinicDayCombo, gbc);

        gbc.gridx = 2; gbc.gridy = 1;
        filterCard.add(searchSlotsBtn, gbc);

        center.add(filterCard);
        center.add(Box.createVerticalStrut(18));

        // Step 2: Available Slots Section
        JPanel slotsSection = new JPanel(new BorderLayout(0, 12));
        slotsSection.setBackground(UITheme.COLOR_SURFACE);
        slotsSection.setBorder(new CompoundBorder(
                new LineBorder(UITheme.COLOR_BORDER, 1, false),
                new EmptyBorder(18, 20, 18, 20)
        ));

        JPanel sectionHeader = new JPanel(new BorderLayout());
        sectionHeader.setOpaque(false);

        JLabel sectionTitle = new JLabel("Available Time Slots (Obtained from Database)");
        sectionTitle.setFont(UITheme.FONT_TITLE_MEDIUM);
        sectionTitle.setForeground(UITheme.COLOR_DEEP_BLUE);

        slotCountLabel = new JLabel("0 slots found");
        slotCountLabel.setFont(UITheme.FONT_SMALL);
        slotCountLabel.setForeground(UITheme.COLOR_TEXT_MUTED);

        sectionHeader.add(sectionTitle, BorderLayout.WEST);
        sectionHeader.add(slotCountLabel, BorderLayout.EAST);
        slotsSection.add(sectionHeader, BorderLayout.NORTH);

        String[] columns = {"Slot ID", "Doctor", "Specialization", "Clinic Date", "Time Window", "Status"};
        slotsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        slotsTable = new ModernTable(slotsTableModel);
        slotsTable.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());
        slotsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = ModernTable.wrapInScrollPane(slotsTable);
        scrollPane.setPreferredSize(new Dimension(800, 220));
        slotsSection.add(scrollPane, BorderLayout.CENTER);

        // Step 3: Booking Form Toolbar
        JPanel bookingActionPanel = new JPanel(new BorderLayout(16, 0));
        bookingActionPanel.setOpaque(false);
        bookingActionPanel.setBorder(new EmptyBorder(12, 0, 0, 0));

        JPanel reasonPanel = new JPanel(new BorderLayout(8, 0));
        reasonPanel.setOpaque(false);
        JLabel reasonLbl = new JLabel("Reason for Visit:");
        reasonLbl.setFont(UITheme.FONT_BODY_BOLD);
        reasonField = new ModernTextField("e.g. Fever, cough, consultation...");
        reasonPanel.add(reasonLbl, BorderLayout.WEST);
        reasonPanel.add(reasonField, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setOpaque(false);

        waitlistButton = new ModernButton("Join Waitlist", ModernButton.StyleType.SECONDARY);
        waitlistButton.addActionListener(e -> handleJoinWaitlist());
        waitlistButton.setToolTipText("If no slots are free, join the queue to be automatically booked if a slot is cancelled.");

        bookButton = new ModernButton("Confirm Booking", ModernButton.StyleType.PRIMARY);
        bookButton.addActionListener(e -> handleConfirmBooking());

        buttonsPanel.add(waitlistButton);
        buttonsPanel.add(bookButton);

        bookingActionPanel.add(reasonPanel, BorderLayout.CENTER);
        bookingActionPanel.add(buttonsPanel, BorderLayout.EAST);

        slotsSection.add(bookingActionPanel, BorderLayout.SOUTH);

        center.add(slotsSection);
        add(new JScrollPane(center), BorderLayout.CENTER);

        loadFilterOptions();
        loadAvailableSlots();
    }

    private void loadFilterOptions() {
        try {
            doctorCombo.removeAllItems();
            doctorCombo.addItem(new DoctorItem(null));
            List<Doctor> docs = DAOFactory.getInstance().getDoctorDAO().getActiveDoctors();
            for (Doctor d : docs) {
                doctorCombo.addItem(new DoctorItem(d));
            }

            clinicDayCombo.removeAllItems();
            clinicDayCombo.addItem(new ClinicDayItem(null));
            List<ClinicDay> days = DAOFactory.getInstance().getClinicDayDAO().getUpcomingClinicDays();
            for (ClinicDay cd : days) {
                clinicDayCombo.addItem(new ClinicDayItem(cd));
            }
        } catch (Exception ex) {
            statusBanner.showMessage("Error loading filter options: " + ex.getMessage(), StatusBanner.Type.ERROR);
        }
    }

    public void loadAvailableSlots() {
        statusBanner.clear();
        slotsTableModel.setRowCount(0);

        DoctorItem selectedDoc = (DoctorItem) doctorCombo.getSelectedItem();
        ClinicDayItem selectedDay = (ClinicDayItem) clinicDayCombo.getSelectedItem();

        Integer docId = (selectedDoc != null && selectedDoc.doctor != null) ? selectedDoc.doctor.getDoctorId() : null;
        LocalDate date = (selectedDay != null && selectedDay.day != null) ? selectedDay.day.getClinicDate() : null;

        try {
            currentAvailableSlots = DAOFactory.getInstance().getSlotDAO().getAvailableSlots(docId, date);
            slotCountLabel.setText(currentAvailableSlots.size() + " available slot(s) found");

            for (Slot s : currentAvailableSlots) {
                slotsTableModel.addRow(new Object[]{
                        "#" + s.getSlotId(),
                        s.getDoctorName() != null ? s.getDoctorName() : "Doctor #" + s.getDoctorId(),
                        s.getSpecialization() != null ? s.getSpecialization() : "General",
                        s.getClinicDate() != null ? s.getClinicDate().toString() : "-",
                        s.getTimeRangeFormatted(),
                        s.getStatus()
                });
            }

            if (currentAvailableSlots.isEmpty()) {
                statusBanner.showMessage("No free slots found for the selected criteria. If clinic day is full, click 'Join Waitlist'.", StatusBanner.Type.INFO);
            }
        } catch (Exception ex) {
            statusBanner.showMessage("Failed to retrieve slots from database: " + ex.getMessage(), StatusBanner.Type.ERROR);
        }
    }

    private void handleConfirmBooking() {
        statusBanner.clear();
        int selectedRow = slotsTable.getSelectedRow();
        if (selectedRow < 0) {
            statusBanner.showMessage("Please select an available time slot from the table.", StatusBanner.Type.WARNING);
            return;
        }

        Object idObj = slotsTableModel.getValueAt(selectedRow, 0);
        int slotId = Integer.parseInt(idObj.toString().replace("#", "").trim());
        String doctor = String.valueOf(slotsTableModel.getValueAt(selectedRow, 1));
        String date = String.valueOf(slotsTableModel.getValueAt(selectedRow, 3));
        String time = String.valueOf(slotsTableModel.getValueAt(selectedRow, 4));
        String reason = reasonField.getText().trim();

        if (reason.isEmpty()) {
            reason = "General Consultation";
        }

        boolean confirmed = ConfirmationDialog.showConfirm(
                SwingUtilities.getWindowAncestor(this),
                "Confirm Clinic Appointment",
                "Please confirm your booking details:<br><br>" +
                        "• <b>Doctor:</b> " + doctor + "<br>" +
                        "• <b>Date:</b> " + date + "<br>" +
                        "• <b>Time:</b> " + time + "<br>" +
                        "• <b>Reason:</b> " + reason + "<br><br>" +
                        "This will call the clinic's atomic stored procedure to secure your slot.",
                "Confirm & Book",
                false
        );

        if (confirmed) {
            try {
                int newApptId = DAOFactory.getInstance().getAppointmentDAO().bookAppointment(
                        currentPatient.getPatientId(),
                        slotId,
                        reason
                );

                JOptionPane.showMessageDialog(this,
                        "Appointment successfully confirmed!\nAppointment ID: #" + newApptId + "\nDate: " + date + " at " + time + "\nDoctor: " + doctor,
                        "Booking Confirmed",
                        JOptionPane.INFORMATION_MESSAGE);

                reasonField.setText("");
                loadAvailableSlots();

                if (callback != null) {
                    callback.onAppointmentBooked();
                }
            } catch (Exception ex) {
                statusBanner.showMessage("Booking could not be completed: " + ex.getMessage(), StatusBanner.Type.ERROR);
                loadAvailableSlots();
            }
        }
    }

    private void handleJoinWaitlist() {
        statusBanner.clear();
        DoctorItem selectedDoc = (DoctorItem) doctorCombo.getSelectedItem();
        ClinicDayItem selectedDay = (ClinicDayItem) clinicDayCombo.getSelectedItem();

        if (selectedDoc == null || selectedDoc.doctor == null) {
            statusBanner.showMessage("Please select a specific Doctor from the dropdown to join their waitlist.", StatusBanner.Type.WARNING);
            doctorCombo.requestFocus();
            return;
        }

        if (selectedDay == null || selectedDay.day == null) {
            statusBanner.showMessage("Please select a specific Clinic Day to join the waitlist.", StatusBanner.Type.WARNING);
            clinicDayCombo.requestFocus();
            return;
        }

        boolean confirmed = ConfirmationDialog.showConfirm(
                SwingUtilities.getWindowAncestor(this),
                "Join Clinic Waitlist",
                "Do you want to join the waitlist for <b>" + selectedDoc.doctor.getFullName() + "</b> on <b>" + selectedDay.day.getClinicDate() + "</b>?<br><br>" +
                        "If another patient cancels their booked appointment, you will automatically be assigned their slot.",
                "Join Waitlist",
                false
        );

        if (confirmed) {
            try {
                int wid = DAOFactory.getInstance().getWaitlistDAO().joinWaitlist(
                        currentPatient.getPatientId(),
                        selectedDoc.doctor.getDoctorId(),
                        selectedDay.day.getClinicDayId()
                );

                statusBanner.showMessage("Successfully added to waitlist! Waitlist ID: #" + wid + ". You will be auto-booked if a slot opens.", StatusBanner.Type.SUCCESS);
            } catch (Exception ex) {
                statusBanner.showMessage("Waitlist error: " + ex.getMessage(), StatusBanner.Type.ERROR);
            }
        }
    }
}
