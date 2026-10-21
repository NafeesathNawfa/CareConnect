package com.careconnect.ui.components;

import com.careconnect.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;

/**
 * A small rounded pill/chip with an optional leading icon. Used for status
 * badges like "Community Free Healthcare" and the "Live Status" tag.
 */
public class Chip extends JPanel {

    private final IconPainter.Icon icon;
    private final String text;
    private final Color bg;
    private final Color fg;
    private final Color borderColor;
    private final boolean dot;
    private Color dotColor;

    public Chip(String text, IconPainter.Icon icon, Color bg, Color fg, Color borderColor) {
        this(text, icon, bg, fg, borderColor, false, null);
    }

    public Chip(String text, IconPainter.Icon icon, Color bg, Color fg, Color borderColor,
                boolean dot, Color dotColor) {
        this.text = text;
        this.icon = icon;
        this.bg = bg;
        this.fg = fg;
        this.borderColor = borderColor;
        this.dot = dot;
        this.dotColor = dotColor;
        setOpaque(false);
    }

    public void setDotColor(Color dotColor) {
        this.dotColor = dotColor;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(Theme.FONT_LABEL);
        int leftPad = (dot ? 20 : (icon != null ? 30 : 14));
        int w = leftPad + fm.stringWidth(text) + 14;
        return new Dimension(w, 28);
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, w, h, h, h);
        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);
        }

        int textX = 14;
        if (dot) {
            g2.setColor(dotColor != null ? dotColor : Theme.SUCCESS);
            g2.fillOval(9, h / 2 - 4, 8, 8);
            textX = 23;
        } else if (icon != null) {
            int is = 14;
            Graphics2D ig = (Graphics2D) g2.create();
            ig.translate(10, (h - is) / 2);
            ig.setColor(fg);
            ig.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            icon.paint(ig, is);
            ig.dispose();
            textX = 30;
        }

        g2.setFont(Theme.FONT_LABEL);
        g2.setColor(fg);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, textX, (h + fm.getAscent() - fm.getDescent()) / 2);
        g2.dispose();
    }
}
