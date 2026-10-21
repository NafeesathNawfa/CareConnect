package com.careconnect.ui.components;

import javax.swing.*;
import java.awt.*;

/**
 * A tiny non-focusable component that paints one of the {@link IconPainter.Icon}
 * glyphs in a chosen color at a fixed size. Keeps icon rendering out of the
 * screens so layouts stay clean.
 */
public class IconLabel extends JComponent {

    private final IconPainter.Icon icon;
    private final int size;
    private Color color;

    public IconLabel(IconPainter.Icon icon, int size, Color color) {
        this.icon = icon;
        this.size = size;
        this.color = color;
        setOpaque(false);
        Dimension d = new Dimension(size, size);
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);
    }

    public void setColor(Color color) {
        this.color = color;
        repaint();
    }

    public int getIconSize() {
        return size;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate((getWidth() - size) / 2, (getHeight() - size) / 2);
        g2.setColor(color);
        icon.paint(g2, size);
        g2.dispose();
    }
}
