package com.careconnect.ui.components;

import com.careconnect.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Full-width rounded primary button with optional leading/trailing icons.
 * Hand-painted (no external dependency), 48px tall, 10px radius, navy fill,
 * white bold text, subtle hover darkening, hand cursor.
 */
public class PrimaryButton extends JButton {

    public enum Variant {
        PRIMARY,    // navy fill, white text
        SECONDARY   // white fill, focused border + secondary text
    }

    private final Variant variant;
    private final IconPainter.Icon leadingIcon;
    private final IconPainter.Icon trailingIcon;
    private boolean hovered;
    private boolean pressed;

    public PrimaryButton(String text) {
        this(text, Variant.PRIMARY, null, null);
    }

    public PrimaryButton(String text, Variant variant) {
        this(text, variant, null, null);
    }

    public PrimaryButton(String text, Variant variant, IconPainter.Icon leadingIcon, IconPainter.Icon trailingIcon) {
        super(text);
        this.variant = variant;
        this.leadingIcon = leadingIcon;
        this.trailingIcon = trailingIcon;

        setFont(Theme.FONT_LABEL.deriveFont(Font.BOLD, 14f));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

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

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(Math.max(d.width, 120), Math.max(d.height, Theme.BUTTON_HEIGHT));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        int radius = Theme.RADIUS_CONTROL;

        Color bg, fg, border = null;
        if (!isEnabled()) {
            bg = Color.decode("#E2E8F0");
            fg = Color.decode("#94A3B8");
        } else if (variant == Variant.PRIMARY) {
            bg = pressed ? Theme.PRIMARY_PRESSED : (hovered ? Theme.PRIMARY_HOVER : Theme.PRIMARY);
            fg = Theme.TEXT_ON_PRIMARY;
        } else {
            bg = hovered ? Theme.INPUT_FILL : Theme.CARD;
            fg = Theme.SECONDARY;
            border = hovered ? Theme.SECONDARY : Theme.INPUT_BORDER;
        }

        g2.setColor(bg);
        g2.fillRoundRect(0, 0, w, h, radius, radius);
        if (border != null) {
            g2.setColor(border);
            g2.drawRoundRect(0, 0, w - 1, h - 1, radius, radius);
        }

        int icon = 18;
        int gap = 10;
        FontMetrics fm = g2.getFontMetrics(getFont());
        int textW = fm.stringWidth(getText());
        int leadW = leadingIcon != null ? icon + gap : 0;
        int trailW = trailingIcon != null ? icon + gap : 0;
        int startX = (w - (textW + leadW + trailW)) / 2;
        int centerY = h / 2;

        g2.setColor(fg);
        g2.setFont(getFont());
        g2.drawString(getText(), startX + leadW, centerY + (fm.getAscent() - fm.getDescent()) / 2);

        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (leadingIcon != null) {
            Graphics2D ig = (Graphics2D) g2.create();
            ig.translate(startX, centerY - icon / 2);
            ig.setColor(fg);
            leadingIcon.paint(ig, icon);
            ig.dispose();
        }
        if (trailingIcon != null) {
            Graphics2D ig = (Graphics2D) g2.create();
            ig.translate(startX + leadW + textW + gap, centerY - icon / 2);
            ig.setColor(fg);
            trailingIcon.paint(ig, icon);
            ig.dispose();
        }

        g2.dispose();
    }
}
