package com.careconnect.ui.components;

import com.careconnect.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

/**
 * Clean, consistent page header displaying title, descriptive subtitle, and action slots.
 */
public class PageHeader extends JPanel {
    private final JLabel titleLabel;
    private final JLabel subtitleLabel;
    private final JPanel actionPanel;

    public PageHeader(String title, String subtitle) {
        setLayout(new BorderLayout());
        setBackground(UITheme.COLOR_SURFACE);
        setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, UITheme.COLOR_BORDER),
                new EmptyBorder(18, 28, 18, 28)
        ));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.FONT_TITLE_LARGE);
        titleLabel.setForeground(UITheme.COLOR_NAVY);

        subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(UITheme.FONT_DESC_ITALIC);
        subtitleLabel.setForeground(UITheme.COLOR_TEXT_MUTED);

        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(subtitleLabel);

        actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        add(textPanel, BorderLayout.WEST);
        add(actionPanel, BorderLayout.EAST);
    }

    public void addAction(JComponent component) {
        actionPanel.add(component);
        actionPanel.revalidate();
        actionPanel.repaint();
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void setSubtitle(String subtitle) {
        subtitleLabel.setText(subtitle);
    }
}
