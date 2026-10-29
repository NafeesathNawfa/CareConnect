package com.careconnect.ui.theme;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

public class ModernButton extends JButton {

    public enum StyleType {
        PRIMARY,   // Deep blue background, white text
        SECONDARY, // White background, ocean blue border & text
        DANGER,    // Crimson background, white text
        LINK       // Transparent background with text hover
    }

    private final StyleType styleType;
    private boolean isHovered = false;
    private boolean isPressed = false;

    public ModernButton(String text) {
        this(text, StyleType.PRIMARY);
    }

    public ModernButton(String text, StyleType styleType) {
        super(text);
        this.styleType = styleType;

        setFont(UITheme.FONT_BODY_BOLD);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(8, 16, 8, 16));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    isHovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    isPressed = true;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(Math.max(d.width + 24, 90), Math.max(d.height + 10, 36));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        Color bgColor;
        Color textColor;
        Color borderColor = null;

        if (!isEnabled()) {
            bgColor = Color.decode("#E2E8F0");
            textColor = Color.decode("#94A3B8");
            borderColor = Color.decode("#CBD5E1");
        } else {
            switch (styleType) {
                case PRIMARY:
                    if (isPressed) bgColor = UITheme.COLOR_NAVY;
                    else if (isHovered) bgColor = UITheme.COLOR_OCEAN_BLUE;
                    else bgColor = UITheme.COLOR_DEEP_BLUE;
                    textColor = UITheme.COLOR_TEXT_ON_DARK;
                    break;
                case SECONDARY:
                    if (isPressed) bgColor = Color.decode("#E5EEF5");
                    else if (isHovered) bgColor = Color.decode("#F0F6FA");
                    else bgColor = UITheme.COLOR_SURFACE;
                    borderColor = isHovered ? UITheme.COLOR_DEEP_BLUE : UITheme.COLOR_OCEAN_BLUE;
                    textColor = isHovered ? UITheme.COLOR_DEEP_BLUE : UITheme.COLOR_OCEAN_BLUE;
                    break;
                case DANGER:
                    if (isPressed) bgColor = Color.decode("#991B1B");
                    else if (isHovered) bgColor = Color.decode("#DC2626");
                    else bgColor = UITheme.COLOR_DANGER;
                    textColor = UITheme.COLOR_TEXT_ON_DARK;
                    break;
                case LINK:
                default:
                    bgColor = isHovered ? Color.decode("#E8F1F8") : new Color(0, 0, 0, 0);
                    textColor = isHovered ? UITheme.COLOR_NAVY : UITheme.COLOR_DEEP_BLUE;
                    break;
            }
        }

        // Clean subtle 4px corner radius for modern rectangular look
        int radius = 4;
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, width, height, radius, radius);

        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, width - 1, height - 1, radius, radius);
        }

        // Draw text centered
        g2.setFont(getFont());
        g2.setColor(textColor);
        FontMetrics fm = g2.getFontMetrics();
        int textX = (width - fm.stringWidth(getText())) / 2;
        int textY = (height + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(getText(), textX, textY);

        g2.dispose();
    }
}
