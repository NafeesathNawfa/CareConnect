package com.careconnect.ui.theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Clean empty state panel shown when tables or lists contain no data.
 */
public class EmptyStatePanel extends JPanel {

    public EmptyStatePanel(String title, String description) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(UITheme.COLOR_SURFACE);
        setBorder(new EmptyBorder(40, 20, 40, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.FONT_TITLE_MEDIUM);
        titleLabel.setForeground(UITheme.COLOR_DEEP_BLUE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(UITheme.FONT_DESC_ITALIC);
        descLabel.setForeground(UITheme.COLOR_TEXT_MUTED);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(Box.createVerticalGlue());
        add(titleLabel);
        add(Box.createVerticalStrut(8));
        add(descLabel);
        add(Box.createVerticalGlue());
    }
}
