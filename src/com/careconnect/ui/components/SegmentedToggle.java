package com.careconnect.ui.components;

import com.careconnect.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * A two-plus option segmented control. Selected segment: navy fill / white text.
 * Unselected: light gray fill / slate text. Purely presentational; consumers
 * register an {@link IntConsumer} to react to selection changes.
 */
public class SegmentedToggle extends JPanel {

    private final List<String> options = new ArrayList<>();
    private int selected = 0;
    private IntConsumer onChange;

    /** Fixed outer height of the whole control, including its 4px padding. */
    public static final int HEIGHT = 48;
    /** Fixed outer padding between the track edge and the segments. */
    public static final int PADDING = 4;
    /** Corner radius shared by the track and the selected pill. */
    private static final int RADIUS = 10;

    public SegmentedToggle(String... options) {
        for (String o : options) this.options.add(o);
        // Equal-width segments, no gap: the padding is the only inset, so the
        // selected pill is identical in size and position for every option.
        setLayout(new GridLayout(1, this.options.size(), 0, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        for (int i = 0; i < this.options.size(); i++) {
            add(new Segment(i));
        }
    }

    /** Locks the control to a fixed height so tab switches can never resize it. */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(super.getPreferredSize().width, HEIGHT);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(super.getMinimumSize().width, HEIGHT);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, HEIGHT);
    }

    public void setOnChange(IntConsumer onChange) {
        this.onChange = onChange;
    }

    public int getSelectedIndex() {
        return selected;
    }

    public void setSelectedIndex(int index) {
        if (index < 0 || index >= options.size() || index == selected) return;
        selected = index;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Theme.NEUTRAL_CHIP_BG);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS, RADIUS);
        g2.dispose();
    }

    private class Segment extends JPanel {
        private final int index;
        private boolean hovered;

        Segment(int index) {
            this.index = index;
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { select(); }
                @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
            });
        }

        private void select() {
            if (index != selected) {
                selected = index;
                SegmentedToggle.this.repaint();
                if (onChange != null) onChange.accept(index);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            boolean active = index == selected;
            Color bg = active ? Theme.PRIMARY : (hovered ? Color.decode("#E6ECF2") : new Color(0, 0, 0, 0));
            Color fg = active ? Theme.TEXT_ON_PRIMARY : Theme.TEXT_BODY;

            if (bg.getAlpha() > 0) {
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS, RADIUS);
            }

            g2.setFont(Theme.FONT_LABEL);
            g2.setColor(fg);
            FontMetrics fm = g2.getFontMetrics();
            String text = options.get(index);
            int x = (getWidth() - fm.stringWidth(text)) / 2;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(text, x, y);

            g2.dispose();
        }
    }
}
