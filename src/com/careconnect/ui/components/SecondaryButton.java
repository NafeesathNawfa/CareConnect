package com.careconnect.ui.components;

import com.careconnect.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Outlined reusable button: white fill, 1px navy border, navy bold text,
 * 10px radius, 48px tall, hand cursor and subtle hover darkening.
 *
 * Used by the redesigned registration screen ("Back to Login", "Reset Form").
 * The preferred width is measured from the label so the text always stays on
 * one line, even in tight layouts.
 */
public class SecondaryButton extends JButton {

    private static final int ICON_SIZE = 18;
    private static final int ICON_GAP = 8;
    private static final int HORIZONTAL_PADDING = 20;

    private final IconPainter.Icon leadingIcon;
    private final IconPainter.Icon trailingIcon;
    private int minWidth = 120;
    private boolean hovered;
    private boolean pressed;

    public SecondaryButton(String text) {
        this(text, null, null);
    }

    public SecondaryButton(String text, IconPainter.Icon leadingIcon, IconPainter.Icon trailingIcon) {
        super(text);
        this.leadingIcon = leadingIcon;
        this.trailingIcon = trailingIcon;

        setFont(Theme.FONT_LABEL.deriveFont(Font.BOLD, 14f));
        setForeground(Theme.PRIMARY);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(0, HORIZONTAL_PADDING, 0, HORIZONTAL_PADDING));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (isEnabled()) { hovered = true; repaint(); }
            }
            @Override public void mouseExited(MouseEvent e) {
                hovered = false; repaint();
            }
            @Override public void mousePressed(MouseEvent e) {
                if (isEnabled()) { pressed = true; repaint(); }
            }
            @Override public void mouseReleased(MouseEvent e) {
                pressed = false; repaint();
            }
        });
    }

    /** Minimum painted width, useful when the design calls for a wider button. */
    public void setMinWidth(int minWidth) {
        this.minWidth = minWidth;
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int textW = fm.stringWidth(getText() == null ? "" : getText());
        int leadW = leadingIcon != null ? ICON_SIZE + ICON_GAP : 0;
        int trailW = trailingIcon != null ? ICON_SIZE + ICON_GAP : 0;
        int width = textW + leadW + trailW + HORIZONTAL_PADDING * 2;
        return new Dimension(Math.max(width, minWidth), Theme.BUTTON_HEIGHT);
    }

    /** Never let the label be squeezed onto a second line / clipped. */
    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int radius = Theme.RADIUS_CONTROL;

        Color bg;
        Color fg;
        if (!isEnabled()) {
            bg = Theme.NEUTRAL_CHIP_BG;
            fg = Theme.TEXT_HINT;
        } else {
            bg = pressed ? Theme.TINT : (hovered ? Theme.INPUT_FILL : Theme.CARD);
            fg = Theme.PRIMARY;
        }

        g2.setColor(bg);
        g2.fillRoundRect(0, 0, w, h, radius, radius);
        g2.setColor(isEnabled() ? Theme.PRIMARY : Theme.CARD_BORDER);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, radius, radius);

        FontMetrics fm = g2.getFontMetrics(getFont());
        int textW = fm.stringWidth(getText() == null ? "" : getText());
        int leadW = leadingIcon != null ? ICON_SIZE + ICON_GAP : 0;
        int trailW = trailingIcon != null ? ICON_SIZE + ICON_GAP : 0;
        int startX = (w - (textW + leadW + trailW)) / 2;
        int centerY = h / 2;

        g2.setColor(fg);
        g2.setFont(getFont());
        g2.drawString(getText(), startX + leadW, centerY + (fm.getAscent() - fm.getDescent()) / 2);

        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (leadingIcon != null) {
            Graphics2D ig = (Graphics2D) g2.create();
            ig.translate(startX, centerY - ICON_SIZE / 2);
            ig.setColor(fg);
            leadingIcon.paint(ig, ICON_SIZE);
            ig.dispose();
        }
        if (trailingIcon != null) {
            Graphics2D ig = (Graphics2D) g2.create();
            ig.translate(startX + leadW + textW + ICON_GAP, centerY - ICON_SIZE / 2);
            ig.setColor(fg);
            trailingIcon.paint(ig, ICON_SIZE);
            ig.dispose();
        }

        g2.dispose();
    }
}
