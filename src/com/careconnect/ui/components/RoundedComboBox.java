package com.careconnect.ui.components;

import com.careconnect.ui.theme.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * Rounded combo box matching the redesigned login inputs: 48px tall,
 * #F3F5F7 fill, 1px #E2E8F0 border that turns navy on focus, and a small
 * chevron drawn on the right with Java2D.
 *
 * The default Swing arrow button is removed so the hand-painted field stays
 * visually identical to {@link RoundedTextField}. Values and selection
 * behaviour are untouched.
 */
public class RoundedComboBox<E> extends JComboBox<E> {

    private boolean focused;
    private Color borderColor = Theme.INPUT_BORDER;

    public RoundedComboBox(E[] items) {
        super(items);

        setOpaque(false);
        setFont(Theme.FONT_BODY);
        setForeground(Theme.TEXT_BODY);
        setBackground(Theme.CARD);
        setFocusable(true);
        setMaximumRowCount(Math.max(3, items.length));
        setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 42));
        setRenderer(new Renderer());
        setUI(new ArrowlessComboUI());

        addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                focused = true;
                borderColor = Theme.INPUT_BORDER_FOCUS;
                repaint();
            }
            @Override public void focusLost(FocusEvent e) {
                focused = false;
                borderColor = Theme.INPUT_BORDER;
                repaint();
            }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, Math.max(d.height, Theme.FIELD_HEIGHT));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D fill = (Graphics2D) g.create();
        fill.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        fill.setColor(Theme.INPUT_FILL);
        fill.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.RADIUS_CONTROL, Theme.RADIUS_CONTROL);
        fill.dispose();

        // Let the UI paint the selected label on top of our fill.
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(focused ? 1.6f : 1f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1,
                Theme.RADIUS_CONTROL, Theme.RADIUS_CONTROL);

        int cx = getWidth() - 24;
        int cy = getHeight() / 2;
        g2.setColor(Theme.TEXT_HINT);
        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(cx - 5, cy - 2, cx, cy + 3);
        g2.drawLine(cx, cy + 3, cx + 5, cy - 2);

        g2.dispose();
    }

    /** Keeps the closed-field label flat/transparent; pads rows inside the popup. */
    private static final class Renderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            label.setFont(Theme.FONT_BODY);
            if (index < 0) {
                label.setOpaque(false);
                label.setForeground(Theme.TEXT_BODY);
                label.setBackground(Theme.CARD);
                label.setBorder(new EmptyBorder(0, 0, 0, 0));
            } else {
                label.setOpaque(true);
                label.setBackground(isSelected ? Theme.TINT : Theme.CARD);
                label.setForeground(isSelected ? Theme.TEXT_ON_TINT : Theme.TEXT_BODY);
                label.setBorder(new EmptyBorder(10, 12, 10, 12));
            }
            return label;
        }
    }

    /** Basic UI with the stock arrow button suppressed (we paint our own chevron). */
    private static final class ArrowlessComboUI extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            JButton hidden = new JButton();
            hidden.setPreferredSize(new Dimension(0, 0));
            hidden.setMinimumSize(new Dimension(0, 0));
            hidden.setMaximumSize(new Dimension(0, 0));
            hidden.setVisible(false);
            hidden.setFocusable(false);
            return hidden;
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            // The rounded fill is painted by RoundedComboBox.paintComponent.
        }
    }
}
