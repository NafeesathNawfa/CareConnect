package com.careconnect.ui.components;

import com.careconnect.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;

/**
 * A reusable rounded panel with an optional soft drop shadow and optional
 * vertical gradient fill. Used as the building block for every login card.
 *
 * Purely presentational: no business logic lives here.
 */
public class RoundedPanel extends JPanel {

    private final int radius;
    private final boolean shadow;
    private final boolean gradient;
    private Color fill;
    private Color borderColor;

    public RoundedPanel() {
        this(Theme.CARD, Theme.RADIUS_CARD, true, false);
    }

    public RoundedPanel(Color fill, int radius, boolean shadow, boolean gradient) {
        this.fill = fill;
        this.radius = radius;
        this.shadow = shadow;
        this.gradient = gradient;
        this.borderColor = null;
        setOpaque(false);
    }

    public void setFill(Color fill) {
        this.fill = fill;
        repaint();
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        repaint();
    }

    private int shadowInset() {
        return shadow ? 10 : 0;
    }

    @Override
    public Insets getInsets() {
        Insets base = super.getInsets();
        int pad = shadowInset();
        return new Insets(base.top + pad, base.left + pad, base.bottom + pad, base.right + pad);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int pad = shadowInset();
        int x = pad;
        int y = pad;
        int w = getWidth() - pad * 2;
        int h = getHeight() - pad * 2;
        if (w <= 0 || h <= 0) {
            g2.dispose();
            return;
        }

        if (shadow) {
            for (int i = pad; i >= 1; i--) {
                float alpha = 0.05f * (1f - (float) i / pad);
                g2.setColor(Theme.shadowAt(alpha));
                g2.fillRoundRect(x - i, y - i + 2, w + i * 2, h + i * 2, radius + i, radius + i);
            }
        }

        if (gradient) {
            g2.setPaint(new GradientPaint(0, y, Theme.HERO_TOP, 0, y + h, Theme.HERO_BOTTOM));
        } else {
            g2.setColor(fill);
        }
        g2.fillRoundRect(x, y, w, h, radius, radius);

        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
        }

        g2.dispose();
    }

    /** Convenience: a white card with a 1px hairline border. */
    public static RoundedPanel card() {
        RoundedPanel p = new RoundedPanel(Theme.CARD, Theme.RADIUS_CARD, true, false);
        p.setBorderColor(Theme.CARD_BORDER);
        return p;
    }

    /** Convenience: a borderless rounded panel of an arbitrary fill. */
    public static RoundedPanel flat(Color fill, int radius) {
        return new RoundedPanel(fill, radius, false, false);
    }

    /** Convenience: the soft light-blue gradient hero container. */
    public static RoundedPanel hero() {
        RoundedPanel p = new RoundedPanel(Theme.HERO_TOP, Theme.RADIUS_CARD, true, true);
        return p;
    }

    public static JPanel box(int axis) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, axis));
        p.setOpaque(false);
        return p;
    }
}
