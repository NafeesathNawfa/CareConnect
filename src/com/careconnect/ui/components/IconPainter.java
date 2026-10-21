package com.careconnect.ui.components;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

/**
 * Tiny dependency-free icon set drawn with Java2D (no network, no image files).
 * Every icon is stroked/filled inside a square box so it can be scaled by size.
 */
public final class IconPainter {

    private IconPainter() {}

    public interface Icon {
        void paint(Graphics2D g, int size);
    }

    private static Graphics2D prep(java.awt.Graphics g, int size) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setStroke(new java.awt.BasicStroke(Math.max(1.5f, size / 12f),
                java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
        return g2;
    }

    /** White medical cross, meant to be drawn on a navy tile. */
    public static final Icon CROSS = (g, size) -> {
        Graphics2D g2 = prep(g, size);
        g2.setColor(java.awt.Color.WHITE);
        float t = size * 0.26f;
        float m = size / 2f;
        g2.fillRoundRect((int) (m - t / 2), (int) (size * 0.16), (int) t, (int) (size * 0.68), (int) (t / 2), (int) (t / 2));
        g2.fillRoundRect((int) (size * 0.16), (int) (m - t / 2), (int) (size * 0.68), (int) t, (int) (t / 2), (int) (t / 2));
        g2.dispose();
    };

    public static final Icon SHIELD = (g, size) -> {
        Graphics2D g2 = prep(g, size);
        Path2D p = new Path2D.Float();
        float w = size, h = size;
        p.moveTo(w * 0.5, h * 0.08);
        p.lineTo(w * 0.86, h * 0.24);
        p.lineTo(w * 0.86, h * 0.52);
        p.quadTo(w * 0.86, h * 0.84, w * 0.5, h * 0.94);
        p.quadTo(w * 0.14, h * 0.84, w * 0.14, h * 0.52);
        p.lineTo(w * 0.14, h * 0.24);
        p.closePath();
        g2.draw(p);
        g2.dispose();
    };

    public static final Icon CLOCK = (g, size) -> {
        Graphics2D g2 = prep(g, size);
        g2.drawOval((int) (size * 0.12), (int) (size * 0.12), (int) (size * 0.76), (int) (size * 0.76));
        g2.drawLine(size / 2, size / 2, size / 2, (int) (size * 0.30));
        g2.drawLine(size / 2, size / 2, (int) (size * 0.68), (int) (size * 0.58));
        g2.dispose();
    };

    public static final Icon ARROW_RIGHT_IN = (g, size) -> {
        Graphics2D g2 = prep(g, size);
        g2.drawLine((int) (size * 0.16), size / 2, (int) (size * 0.80), size / 2);
        g2.drawLine((int) (size * 0.55), (int) (size * 0.25), (int) (size * 0.80), size / 2);
        g2.drawLine((int) (size * 0.55), (int) (size * 0.75), (int) (size * 0.80), size / 2);
        g2.dispose();
    };

    public static final Icon PHONE = (g, size) -> {
        Graphics2D g2 = prep(g, size);
        g2.drawRoundRect((int) (size * 0.30), (int) (size * 0.12), (int) (size * 0.40), (int) (size * 0.76), (int) (size * 0.12), (int) (size * 0.12));
        g2.fillOval((int) (size * 0.45), (int) (size * 0.78), (int) (size * 0.10), (int) (size * 0.10));
        g2.dispose();
    };

    public static final Icon LOCK = (g, size) -> {
        Graphics2D g2 = prep(g, size);
        g2.drawRoundRect((int) (size * 0.22), (int) (size * 0.45), (int) (size * 0.56), (int) (size * 0.42), (int) (size * 0.10), (int) (size * 0.10));
        g2.drawArc((int) (size * 0.32), (int) (size * 0.16), (int) (size * 0.36), (int) (size * 0.48), 0, 180);
        g2.dispose();
    };

    public static final Icon CHECK = (g, size) -> {
        Graphics2D g2 = prep(g, size);
        g2.drawLine((int) (size * 0.22), (int) (size * 0.54), (int) (size * 0.42), (int) (size * 0.74));
        g2.drawLine((int) (size * 0.42), (int) (size * 0.74), (int) (size * 0.80), (int) (size * 0.28));
        g2.dispose();
    };

    public static final Icon DOC = (g, size) -> {
        Graphics2D g2 = prep(g, size);
        g2.drawRoundRect((int) (size * 0.24), (int) (size * 0.12), (int) (size * 0.52), (int) (size * 0.76), (int) (size * 0.08), (int) (size * 0.08));
        g2.drawLine((int) (size * 0.34), (int) (size * 0.38), (int) (size * 0.66), (int) (size * 0.38));
        g2.drawLine((int) (size * 0.34), (int) (size * 0.54), (int) (size * 0.66), (int) (size * 0.54));
        g2.dispose();
    };

    public static final Icon QUEUE = (g, size) -> {
        Graphics2D g2 = prep(g, size);
        int r = (int) (size * 0.14);
        g2.drawOval((int) (size * 0.12), (int) (size * 0.20), r, r);
        g2.drawLine((int) (size * 0.36), (int) (size * 0.27), (int) (size * 0.86), (int) (size * 0.27));
        g2.drawOval((int) (size * 0.12), (int) (size * 0.58), r, r);
        g2.drawLine((int) (size * 0.36), (int) (size * 0.65), (int) (size * 0.86), (int) (size * 0.65));
        g2.dispose();
    };

    public static final Icon USER = (g, size) -> {
        Graphics2D g2 = prep(g, size);
        g2.drawOval((int) (size * 0.34), (int) (size * 0.14), (int) (size * 0.32), (int) (size * 0.32));
        Path2D p = new Path2D.Float();
        p.moveTo(size * 0.18, size * 0.88);
        p.quadTo(size * 0.5, size * 0.52, size * 0.82, size * 0.88);
        g2.draw(p);
        g2.dispose();
    };

    public static final Icon STETHOSCOPE = (g, size) -> {
        Graphics2D g2 = prep(g, size);
        g2.drawArc((int) (size * 0.18), (int) (size * 0.14), (int) (size * 0.44), (int) (size * 0.52), 0, 180);
        g2.drawLine((int) (size * 0.18), (int) (size * 0.40), (int) (size * 0.18), (int) (size * 0.58));
        g2.drawLine((int) (size * 0.62), (int) (size * 0.40), (int) (size * 0.62), (int) (size * 0.62));
        g2.drawOval((int) (size * 0.50), (int) (size * 0.60), (int) (size * 0.26), (int) (size * 0.26));
        g2.dispose();
    };
}
