package com.careconnect.ui.components;

import com.careconnect.dao.DAOFactory;
import com.careconnect.model.Patient;
import com.careconnect.model.Staff;
import com.careconnect.ui.theme.TypewriterHoverLabel;
import com.careconnect.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern navy sidebar navigation supporting typewriter hover effects,
 * role-based menu options, and active state tracking.
 */
public class SidebarNav extends JPanel {

    public interface NavListener {
        void onNavSelected(String screenKey);
        void onLogout();
    }

    private final NavListener listener;
    private final JPanel menuItemsContainer;
    private final List<NavItemPanel> navItemPanels = new ArrayList<>();
    private String activeKey = "";

    public SidebarNav(NavListener listener) {
        this.listener = listener;
        setPreferredSize(new Dimension(240, 700));
        setBackground(UITheme.COLOR_NAVY);
        setLayout(new BorderLayout());

        // Brand / Top Header
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new EmptyBorder(24, 20, 24, 20));

        JLabel titleLabel = new JLabel("CareConnect");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(UITheme.COLOR_TEXT_ON_DARK);

        JLabel subLabel = new JLabel("CLINIC APPOINTMENT SYSTEM");
        subLabel.setFont(new Font("Segoe UI", Font.BOLD, 9));
        subLabel.setForeground(UITheme.COLOR_SKY_BLUE);

        brandPanel.add(titleLabel);
        brandPanel.add(Box.createVerticalStrut(4));
        brandPanel.add(subLabel);

        add(brandPanel, BorderLayout.NORTH);

        // Menu Items list
        menuItemsContainer = new JPanel();
        menuItemsContainer.setLayout(new BoxLayout(menuItemsContainer, BoxLayout.Y_AXIS));
        menuItemsContainer.setOpaque(false);
        menuItemsContainer.setBorder(new EmptyBorder(10, 0, 10, 0));

        JScrollPane scrollPane = new JScrollPane(menuItemsContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom status & logout
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(16, 16, 20, 16));

        // Data Layer status tag
        String dataStatus = DAOFactory.getInstance().isLiveDatabaseActive() ? "● MySQL Live" : "○ Memory Mode";
        JLabel dbStatusLabel = new JLabel(dataStatus);
        dbStatusLabel.setFont(UITheme.FONT_SMALL);
        dbStatusLabel.setForeground(DAOFactory.getInstance().isLiveDatabaseActive() ? Color.decode("#81C784") : UITheme.COLOR_SKY_BLUE);
        bottomPanel.add(dbStatusLabel);
        bottomPanel.add(Box.createVerticalStrut(12));

        // Logout action
        TypewriterHoverLabel logoutLabel = new TypewriterHoverLabel("← Switch / Logout", UITheme.COLOR_SKY_BLUE, Color.WHITE, UITheme.FONT_BODY_BOLD);
        logoutLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (listener != null) listener.onLogout();
            }
        });
        bottomPanel.add(logoutLabel);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void configureForPatient(Patient patient) {
        menuItemsContainer.removeAll();
        navItemPanels.clear();

        // Patient menu
        addNavItem("patient_dashboard", "Dashboard Overview");
        addNavItem("book_appointment", "Book Appointment");
        addNavItem("my_appointments", "My Appointments");

        menuItemsContainer.revalidate();
        menuItemsContainer.repaint();
    }

    public void configureForStaff(Staff staff) {
        menuItemsContainer.removeAll();
        navItemPanels.clear();

        addNavItem("admin_appointments", "Appointment Registry");
        addNavItem("doctor_schedule", "Doctor Schedules");
        addNavItem("manage_doctors", "Doctor Management");
        addNavItem("manage_days", "Clinic Days & Slots");
        addNavItem("manage_waitlist", "Waitlist Queue");

        menuItemsContainer.revalidate();
        menuItemsContainer.repaint();
    }

    private void addNavItem(String key, String title) {
        NavItemPanel item = new NavItemPanel(key, title);
        navItemPanels.add(item);
        menuItemsContainer.add(item);
        menuItemsContainer.add(Box.createVerticalStrut(4));
    }

    public void setActive(String key) {
        this.activeKey = key;
        for (NavItemPanel item : navItemPanels) {
            item.setSelected(item.key.equals(key));
        }
    }

    private class NavItemPanel extends JPanel {
        private final String key;
        private boolean isSelected = false;
        private boolean isHovered = false;
        private final TypewriterHoverLabel label;

        public NavItemPanel(String key, String title) {
            this.key = key;
            setLayout(new BorderLayout());
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 20, 10, 20));

            label = new TypewriterHoverLabel(title, Color.decode("#D3DFE8"), Color.WHITE, UITheme.FONT_BODY);
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    select();
                }
            });
            add(label, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    select();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }

        private void select() {
            setActive(key);
            if (listener != null) listener.onNavSelected(key);
        }

        public void setSelected(boolean sel) {
            this.isSelected = sel;
            label.setForeground(sel ? Color.WHITE : Color.decode("#D3DFE8"));
            label.setFont(sel ? UITheme.FONT_BODY_BOLD : UITheme.FONT_BODY);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (isSelected || isHovered) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isSelected) {
                    g2.setColor(UITheme.COLOR_DEEP_BLUE);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    // Left active indicator strip in sky blue
                    g2.setColor(UITheme.COLOR_SKY_BLUE);
                    g2.fillRect(0, 0, 4, getHeight());
                } else if (isHovered) {
                    g2.setColor(new Color(31, 78, 121, 100));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.dispose();
            }
        }
    }
}
