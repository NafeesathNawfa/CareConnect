package com.careconnect.ui.theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * An alert banner for showing inline success, warning, validation, or error messages.
 */
public class StatusBanner extends JPanel {

    public enum Type {
        SUCCESS,
        WARNING,
        ERROR,
        INFO
    }

    private final JLabel messageLabel;
    private final JButton closeButton;

    public StatusBanner() {
        setLayout(new BorderLayout(10, 0));
        setBorder(new EmptyBorder(10, 16, 10, 16));
        setVisible(false);

        messageLabel = new JLabel("");
        messageLabel.setFont(UITheme.FONT_BODY);

        closeButton = new JButton("×");
        closeButton.setFont(new Font("Arial", Font.BOLD, 16));
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setFocusPainted(false);
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> setVisible(false));

        add(messageLabel, BorderLayout.CENTER);
        add(closeButton, BorderLayout.EAST);
    }

    public void showMessage(String message, Type type) {
        messageLabel.setText(message);

        switch (type) {
            case SUCCESS:
                setBackground(UITheme.COLOR_SUCCESS_BG);
                messageLabel.setForeground(UITheme.COLOR_SUCCESS);
                closeButton.setForeground(UITheme.COLOR_SUCCESS);
                break;
            case WARNING:
                setBackground(UITheme.COLOR_WARNING_BG);
                messageLabel.setForeground(UITheme.COLOR_WARNING);
                closeButton.setForeground(UITheme.COLOR_WARNING);
                break;
            case ERROR:
                setBackground(UITheme.COLOR_DANGER_BG);
                messageLabel.setForeground(UITheme.COLOR_DANGER);
                closeButton.setForeground(UITheme.COLOR_DANGER);
                break;
            case INFO:
            default:
                setBackground(Color.decode("#EBF3FA"));
                messageLabel.setForeground(UITheme.COLOR_DEEP_BLUE);
                closeButton.setForeground(UITheme.COLOR_DEEP_BLUE);
                break;
        }

        setVisible(true);
        revalidate();
        repaint();
    }

    public void clear() {
        setVisible(false);
        messageLabel.setText("");
    }
}
