package com.careconnect.ui.theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * Modern rectangular text input field with placeholder and focus highlight.
 */
public class ModernTextField extends JTextField {
    private String placeholder;
    private boolean isFocused = false;

    public ModernTextField() {
        this("");
    }

    public ModernTextField(String placeholder) {
        this.placeholder = placeholder;
        setFont(UITheme.FONT_BODY);
        setForeground(UITheme.COLOR_TEXT_PRIMARY);
        setBackground(UITheme.COLOR_SURFACE);
        setCaretColor(UITheme.COLOR_DEEP_BLUE);
        setBorder(new EmptyBorder(8, 12, 8, 12));

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                isFocused = true;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                isFocused = false;
                repaint();
            }
        });
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, Math.max(d.height, 36));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Background
        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());

        super.paintComponent(g);

        // Placeholder text
        if (getText().isEmpty() && placeholder != null && !placeholder.isEmpty() && !isFocused) {
            g2.setFont(UITheme.FONT_BODY);
            g2.setColor(UITheme.COLOR_TEXT_MUTED);
            FontMetrics fm = g2.getFontMetrics();
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(placeholder, getInsets().left, y);
        }

        g2.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (isFocused) {
            g2.setColor(UITheme.COLOR_SOFT_BLUE);
            g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            g2.setColor(new Color(111, 163, 216, 70));
            g2.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
        } else {
            g2.setColor(UITheme.COLOR_BORDER);
            g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        }

        g2.dispose();
    }
}
