package com.careconnect;

import com.careconnect.dao.DAOFactory;
import com.careconnect.ui.screens.MainFrame;

import javax.swing.*;

/**
 * Main application entry point for CareConnect Clinic Appointment Management System.
 */
public class Main {
    public static void main(String[] args) {
        // Enable high-DPI scaling and text antialiasing
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            // Set System Look and Feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Sensible fallback to default Swing L&F
        }

        System.out.println("==================================================");
        System.out.println(" CareConnect - Clinic Appointment Management System");
        System.out.println(" " + DAOFactory.getInstance().getDataLayerStatusDescription());
        System.out.println("==================================================");

        SwingUtilities.invokeLater(() -> {
            MainFrame app = new MainFrame();
            app.setVisible(true);
        });
    }
}
