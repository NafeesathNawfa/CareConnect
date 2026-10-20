package com.careconnect.ui.components;

import com.careconnect.ui.theme.ModernButton;
import com.careconnect.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modern modal confirmation dialog styled in harmony with the project palette.
 */
public class ConfirmationDialog extends JDialog {
    private boolean confirmed = false;

    public ConfirmationDialog(Window owner, String title, String message, String confirmText, boolean isDanger) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.COLOR_SURFACE);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UITheme.COLOR_SURFACE);
        content.setBorder(new EmptyBorder(24, 24, 20, 24));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_TITLE_MEDIUM);
        titleLbl.setForeground(isDanger ? UITheme.COLOR_DANGER : UITheme.COLOR_NAVY);

        JLabel msgLbl = new JLabel("<html><body style='width: 300px;'>" + message + "</body></html>");
        msgLbl.setFont(UITheme.FONT_BODY);
        msgLbl.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        content.add(titleLbl);
        content.add(Box.createVerticalStrut(12));
        content.add(msgLbl);

        add(content, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 16));
        buttonPanel.setBackground(UITheme.COLOR_BG_CANVAS);
        buttonPanel.setBorder(new EmptyBorder(0, 16, 0, 16));

        ModernButton cancelBtn = new ModernButton("Cancel", ModernButton.StyleType.SECONDARY);
        cancelBtn.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        ModernButton confirmBtn = new ModernButton(
                confirmText,
                isDanger ? ModernButton.StyleType.DANGER : ModernButton.StyleType.PRIMARY
        );
        confirmBtn.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        buttonPanel.add(cancelBtn);
        buttonPanel.add(confirmBtn);

        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    public static boolean showConfirm(Window owner, String title, String message, String confirmText, boolean isDanger) {
        ConfirmationDialog dlg = new ConfirmationDialog(owner, title, message, confirmText, isDanger);
        dlg.setVisible(true);
        return dlg.confirmed;
    }
}
