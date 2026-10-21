package com.careconnect.ui.screens;

import com.careconnect.dao.DAOFactory;
import com.careconnect.model.ClinicDay;
import com.careconnect.ui.components.Chip;
import com.careconnect.ui.components.IconLabel;
import com.careconnect.ui.components.IconPainter;
import com.careconnect.ui.components.RoundedPanel;
import com.careconnect.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Presentational building blocks shared by the redesigned login screens:
 * the top brand bar, the patient hero panel (with real DB data), and the
 * staff/provider info column.
 *
 * Contains no authentication logic; it only reads aggregate data through the
 * existing DAOs (read-only) to populate real counts and upcoming clinic days.
 */
final class LoginComponents {

    private LoginComponents() {}

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("EEE, dd MMM");

    // ------------------------------------------------------------------
    // Top brand bar
    // ------------------------------------------------------------------
    static JPanel topBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.CARD);
        bar.setBorder(new EmptyBorder(16, Theme.SPACE_XL, 16, Theme.SPACE_XL));
        bar.setPreferredSize(new Dimension(0, 80));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JPanel logo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.translate(getWidth() / 2 - 13, getHeight() / 2 - 13);
                IconPainter.CROSS.paint(g2, 26);
                g2.dispose();
            }
        };
        logo.setOpaque(false);
        logo.setPreferredSize(new Dimension(40, 40));
        logo.setMaximumSize(new Dimension(40, 40));

        JLabel name = new JLabel("CareConnect");
        name.setFont(Theme.FONT_HEADING);
        name.setForeground(Theme.PRIMARY);

        left.add(logo);
        left.add(name);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        boolean live = DAOFactory.getInstance().isLiveDatabaseActive();
        Chip status = new Chip(live ? "MySQL Live" : "Memory Mode", null,
                live ? Theme.SUCCESS_BG : Theme.WARNING_BG,
                live ? Theme.SUCCESS : Theme.WARNING,
                live ? Theme.SUCCESS_BG : Theme.WARNING_BG, true,
                live ? Theme.SUCCESS : Theme.WARNING);
        right.add(status);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ------------------------------------------------------------------
    // Patient hero (left column)
    // ------------------------------------------------------------------
    static JPanel patientHero() {
        RoundedPanel hero = RoundedPanel.hero();
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        hero.setBorder(new EmptyBorder(Theme.SPACE_XL, Theme.SPACE_XL, Theme.SPACE_XL, Theme.SPACE_XL));

        Chip tag = new Chip("Community Free Healthcare", IconPainter.CHECK, Theme.CARD, Theme.TEXT_ON_TINT, Theme.TINT_BORDER);
        tag.setAlignmentX(Component.LEFT_ALIGNMENT);
        hero.add(tag);
        hero.add(Box.createVerticalStrut(Theme.SPACE_LG));

        JLabel heading = new JLabel("<html>Accessible Care,<br>Streamlined Operations</html>");
        heading.setFont(Theme.FONT_DISPLAY);
        heading.setForeground(Theme.PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        hero.add(heading);
        hero.add(Box.createVerticalStrut(Theme.SPACE_MD));

        JLabel para = new JLabel("<html><body style='width:420px'>Your health matters. Sign in to book appointments, "
                + "connect with doctors, and access free healthcare services quickly and easily. We're here to make "
                + "quality healthcare accessible for everyone.</body></html>");
        para.setFont(Theme.FONT_BODY);
        para.setForeground(Theme.TEXT_BODY);
        para.setAlignmentX(Component.LEFT_ALIGNMENT);
        hero.add(para);
        hero.add(Box.createVerticalStrut(Theme.SPACE_LG));

        hero.add(clinicTodayCard());
        hero.add(Box.createVerticalStrut(Theme.SPACE_MD));
        hero.add(featureRow());
        return hero;
    }

    /** Real clinic days pulled read-only from the DB (no fabricated times). */
    private static JPanel clinicTodayCard() {
        RoundedPanel card = RoundedPanel.card();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);
        titleRow.add(new IconLabel(IconPainter.CLOCK, 18, Theme.SECONDARY));
        JLabel title = new JLabel("Upcoming Clinic Days");
        title.setFont(Theme.FONT_LABEL.deriveFont(Font.BOLD, 14f));
        title.setForeground(Theme.PRIMARY);
        titleRow.add(title);
        header.add(titleRow, BorderLayout.WEST);

        List<ClinicDay> days = safeUpcomingDays();
        boolean anyOpen = days.stream().anyMatch(ClinicDay::isOpen);
        Chip live = new Chip(anyOpen ? "Sessions Scheduled" : "No Open Sessions", null,
                anyOpen ? Theme.SUCCESS_BG : Theme.WARNING_BG,
                anyOpen ? Theme.SUCCESS : Theme.WARNING,
                Theme.CARD, true, anyOpen ? Theme.SUCCESS : Theme.WARNING);
        header.add(live, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        JPanel tiles = new JPanel(new GridLayout(1, Math.max(days.size(), 1), 12, 0));
        tiles.setOpaque(false);
        if (days.isEmpty()) {
            JLabel none = new JLabel("No clinic days scheduled yet.");
            none.setFont(Theme.FONT_SMALL);
            none.setForeground(Theme.TEXT_HINT);
            tiles.add(none);
        } else {
            for (ClinicDay d : days) {
                tiles.add(clinicTile(d));
            }
        }
        card.add(tiles, BorderLayout.CENTER);
        return card;
    }

    private static JPanel clinicTile(ClinicDay d) {
        RoundedPanel tile = RoundedPanel.flat(d.isOpen() ? Theme.TINT : Theme.NEUTRAL_CHIP_BG, 12);
        tile.setLayout(new BoxLayout(tile, BoxLayout.Y_AXIS));
        tile.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel date = new JLabel(d.getClinicDate() != null ? d.getClinicDate().format(DATE_FMT) : "-");
        date.setFont(Theme.FONT_LABEL);
        date.setForeground(Theme.PRIMARY);
        date.setAlignmentX(Component.LEFT_ALIGNMENT);

        String notes = d.getNotes() != null && !d.getNotes().isBlank() ? d.getNotes() : (d.isOpen() ? "General OPD" : "Closed");
        JLabel note = new JLabel("<html><body style='width:120px'>" + notes + "</body></html>");
        note.setFont(Theme.FONT_SMALL);
        note.setForeground(Theme.TEXT_BODY);
        note.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        statusRow.setOpaque(false);
        statusRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(d.isOpen() ? Theme.SUCCESS : Theme.TEXT_HINT);
                g2.fillOval(0, 3, 8, 8);
                g2.dispose();
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(8, 14));
        JLabel status = new JLabel(d.isOpen() ? "Open" : "Closed");
        status.setFont(Theme.FONT_SMALL);
        status.setForeground(d.isOpen() ? Theme.SUCCESS : Theme.TEXT_HINT);
        statusRow.add(dot);
        statusRow.add(status);

        tile.add(date);
        tile.add(Box.createVerticalStrut(4));
        tile.add(note);
        tile.add(Box.createVerticalStrut(6));
        tile.add(statusRow);
        return tile;
    }

    private static JPanel featureRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        row.add(featureCard(IconPainter.CHECK, "Verified Free Consults", "Zero cost consultations with licensed medical staff."));
        row.add(featureCard(IconPainter.DOC, "Digital Rx History", "Fast pharmacy dispensing and clear e-prescriptions."));
        row.add(featureCard(IconPainter.QUEUE, "OPD Triage Tokens", "Real-time wait estimates and online check-in queue."));
        return row;
    }

    private static JPanel featureCard(IconPainter.Icon icon, String title, String desc) {
        RoundedPanel card = RoundedPanel.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel badge = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.TINT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.translate((getWidth() - 18) / 2, (getHeight() - 18) / 2);
                g2.setColor(Theme.PRIMARY);
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                icon.paint(g2, 18);
                g2.dispose();
            }
        };
        badge.setOpaque(false);
        badge.setPreferredSize(new Dimension(36, 36));
        badge.setMaximumSize(new Dimension(36, 36));
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel t = new JLabel(title);
        t.setFont(Theme.FONT_LABEL);
        t.setForeground(Theme.PRIMARY);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel d = new JLabel("<html><body style='width:150px'>" + desc + "</body></html>");
        d.setFont(Theme.FONT_SMALL);
        d.setForeground(Theme.TEXT_HINT);
        d.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(badge);
        card.add(Box.createVerticalStrut(10));
        card.add(t);
        card.add(Box.createVerticalStrut(4));
        card.add(d);
        return card;
    }

    // ------------------------------------------------------------------
    // Staff column pieces
    // ------------------------------------------------------------------
    static JPanel staffInfoColumn() {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);

        // Dark navy card
        RoundedPanel navy = new RoundedPanel(Theme.PRIMARY, Theme.RADIUS_CARD, true, false);
        navy.setLayout(new BoxLayout(navy, BoxLayout.Y_AXIS));
        navy.setBorder(new EmptyBorder(Theme.SPACE_XL, Theme.SPACE_XL, Theme.SPACE_XL, Theme.SPACE_XL));
        navy.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel logo = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.translate((getWidth() - 24) / 2, (getHeight() - 24) / 2);
                g2.setColor(Theme.PRIMARY);
                IconPainter.CROSS.paint(g2, 24);
                g2.dispose();
            }
        };
        logo.setOpaque(false);
        logo.setPreferredSize(new Dimension(44, 44));
        logo.setMaximumSize(new Dimension(44, 44));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Free Clinic System");
        title.setFont(Theme.FONT_TITLE.deriveFont(Font.BOLD, 24f));
        title.setForeground(Theme.TEXT_ON_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("CLINICAL & PROVIDER OPERATIONS");
        sub.setFont(Theme.FONT_SMALL.deriveFont(Font.BOLD, 11f));
        sub.setForeground(Theme.TINT);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel desc = new JLabel("<html><body style='width:360px'>Access patient records, manage clinic days and slots, "
                + "and configure appointment availability.</body></html>");
        desc.setFont(Theme.FONT_BODY);
        desc.setForeground(new Color(255, 255, 255, 220));
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);

        navy.add(logo);
        navy.add(Box.createVerticalStrut(Theme.SPACE_MD));
        navy.add(title);
        navy.add(Box.createVerticalStrut(4));
        navy.add(sub);
        navy.add(Box.createVerticalStrut(Theme.SPACE_MD));
        navy.add(desc);
        navy.add(Box.createVerticalStrut(Theme.SPACE_LG));

        // Real stat tiles (translucent)
        int activeDoctors = safeActiveDoctorCount();
        int staffAccounts = safeStaffCount();
        JPanel stats = new JPanel(new GridLayout(1, 2, 12, 0));
        stats.setOpaque(false);
        stats.setAlignmentX(Component.LEFT_ALIGNMENT);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        stats.add(statTile(String.valueOf(activeDoctors), "Active Doctors"));
        stats.add(statTile(String.valueOf(staffAccounts), "Staff Accounts"));
        navy.add(stats);

        col.add(navy);
        col.add(Box.createVerticalStrut(Theme.SPACE_MD));

        // Honest privacy note
        RoundedPanel note = RoundedPanel.card();
        note.setLayout(new BorderLayout(10, 0));
        note.setBorder(new EmptyBorder(16, 16, 16, 16));
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        note.add(new IconLabel(IconPainter.SHIELD, 20, Theme.SECONDARY), BorderLayout.WEST);
        JLabel noteText = new JLabel("<html><body style='width:360px'>Patient and appointment data is stored in the clinic's "
                + "MySQL database. Appointment status changes are recorded in an audit log (appointment_log).</body></html>");
        noteText.setFont(Theme.FONT_SMALL);
        noteText.setForeground(Theme.TEXT_BODY);
        note.add(noteText, BorderLayout.CENTER);
        col.add(note);
        col.add(Box.createVerticalStrut(Theme.SPACE_MD));

        // Real table-name chips
        RoundedPanel tablesCard = RoundedPanel.card();
        tablesCard.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 6));
        tablesCard.setBorder(new EmptyBorder(12, 12, 12, 12));
        tablesCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (String t : new String[]{"DOCTOR", "SLOT", "CLINIC_DAY", "APPOINTMENT", "PATIENT", "STAFF"}) {
            JLabel chip = new JLabel(t);
            chip.setFont(Theme.FONT_MONO);
            chip.setForeground(Theme.SECONDARY);
            chip.setOpaque(true);
            chip.setBackground(Theme.NEUTRAL_CHIP_BG);
            chip.setBorder(new EmptyBorder(3, 8, 3, 8));
            tablesCard.add(chip);
        }
        col.add(tablesCard);
        return col;
    }

    private static JPanel statTile(String value, String label) {
        RoundedPanel tile = new RoundedPanel(new Color(255, 255, 255, 40), 12, false, false);
        tile.setLayout(new BoxLayout(tile, BoxLayout.Y_AXIS));
        tile.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel v = new JLabel(value);
        v.setFont(Theme.FONT_HEADING.deriveFont(Font.BOLD, 22f));
        v.setForeground(Theme.TEXT_ON_PRIMARY);
        v.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel l = new JLabel(label);
        l.setFont(Theme.FONT_SMALL);
        l.setForeground(Theme.TINT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);

        tile.add(v);
        tile.add(Box.createVerticalStrut(2));
        tile.add(l);
        return tile;
    }

    // ------------------------------------------------------------------
    // Read-only data accessors (never throw; degrade gracefully)
    // ------------------------------------------------------------------
    private static List<ClinicDay> safeUpcomingDays() {
        try {
            List<ClinicDay> days = DAOFactory.getInstance().getClinicDayDAO().getUpcomingClinicDays();
            return days.size() > 3 ? days.subList(0, 3) : days;
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

    private static int safeActiveDoctorCount() {
        try {
            return DAOFactory.getInstance().getDoctorDAO().getActiveDoctors().size();
        } catch (Exception e) {
            return 0;
        }
    }

    private static int safeStaffCount() {
        try {
            return DAOFactory.getInstance().getStaffDAO().getAllStaff().size();
        } catch (Exception e) {
            return 0;
        }
    }
}
