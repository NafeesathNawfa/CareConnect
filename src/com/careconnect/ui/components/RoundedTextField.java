package com.careconnect.ui.components;

import com.careconnect.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * Rounded text field with optional leading icon, placeholder, and a navy focus
 * border. Reusable across the login screens (and later elsewhere).
 */
public class RoundedTextField extends JTextField {

    private final String placeholder;
    private final int radius;
    private final int iconSize;
    private final IconPainter.Icon leadingIcon;
    private IconPainter.Icon trailingIcon;
    private boolean focused;
    private Color borderColor;
    private Color iconColor;

    public RoundedTextField(String placeholder) {
        this(placeholder, null);
    }

    public RoundedTextField(String placeholder, IconPainter.Icon leadingIcon) {
        this(placeholder, leadingIcon, Theme.RADIUS_CONTROL);
    }

    public RoundedTextField(String placeholder, IconPainter.Icon leadingIcon, int radius) {
        this.placeholder = placeholder;
        this.leadingIcon = leadingIcon;
        this.radius = radius;
        this.iconSize = 18;
        this.borderColor = Theme.INPUT_BORDER;
        this.iconColor = Theme.TEXT_HINT;

        setOpaque(false);
        setFont(Theme.FONT_BODY);
        setForeground(Theme.TEXT_BODY);
        setCaretColor(Theme.PRIMARY);
        setSelectionColor(Theme.TINT);
        applyInsets();

        addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                focused = true;
                setBorderColor(Theme.INPUT_BORDER_FOCUS);
                iconColor = Theme.PRIMARY;
                repaint();
            }
            @Override public void focusLost(FocusEvent e) {
                focused = false;
                setBorderColor(Theme.INPUT_BORDER);
                iconColor = Theme.TEXT_HINT;
                repaint();
            }
        });
    }

    public void setBorderColor(Color c) {
        this.borderColor = c;
        repaint();
    }

    /** Adds (or replaces) a decorative icon drawn on the right inside the field. */
    public void setTrailingIcon(IconPainter.Icon icon) {
        this.trailingIcon = icon;
        applyInsets();
        revalidate();
        repaint();
    }

    private void applyInsets() {
        int left = leadingIcon != null ? 44 : 16;
        int right = trailingIcon != null ? 44 : 14;
        setBorder(BorderFactory.createEmptyBorder(0, left, 0, right));
    }

    /** Paints a red ring to indicate a validation error. */
    public void markError(boolean error) {
        setBorderColor(error ? Theme.DANGER : (focused ? Theme.INPUT_BORDER_FOCUS : Theme.INPUT_BORDER));
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, Math.max(d.height, Theme.FIELD_HEIGHT));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(Theme.INPUT_FILL);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

        if (leadingIcon != null) {
            Graphics2D ig = (Graphics2D) g2.create();
            ig.translate(14, (getHeight() - iconSize) / 2);
            ig.setColor(iconColor);
            ig.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            leadingIcon.paint(ig, iconSize);
            ig.dispose();
        }

        if (trailingIcon != null) {
            Graphics2D ig = (Graphics2D) g2.create();
            ig.translate(getWidth() - 14 - iconSize, (getHeight() - iconSize) / 2);
            ig.setColor(iconColor);
            ig.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            trailingIcon.paint(ig, iconSize);
            ig.dispose();
        }

        super.paintComponent(g2);

        if (getText().isEmpty() && placeholder != null && !placeholder.isEmpty() && !focused) {
            g2.setFont(Theme.FONT_BODY);
            g2.setColor(Theme.TEXT_HINT);
            FontMetrics fm = g2.getFontMetrics();
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(placeholder, getInsets().left, y);
        }

        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(focused || borderColor == Theme.DANGER ? 1.6f : 1f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

        g2.dispose();
    }
}
