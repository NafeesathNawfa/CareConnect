package com.careconnect.ui.theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * An interactive label that displays text with a refined typewriter reveal effect on hover.
 * Carefully engineered to prevent layout shifts by pre-calculating the full text dimensions.
 * Resets cleanly to full text on mouse exit.
 */
public class TypewriterHoverLabel extends JLabel {
    private final String fullText;
    private int visibleChars;
    private Timer typewriterTimer;
    private boolean isHovered = false;
    private Color normalColor;
    private Color hoverColor;
    private boolean typewriterEnabled = true;

    public TypewriterHoverLabel(String text) {
        this(text, UITheme.COLOR_TEXT_PRIMARY, UITheme.COLOR_DEEP_BLUE, UITheme.FONT_BODY);
    }

    public TypewriterHoverLabel(String text, Color normalColor, Color hoverColor, Font font) {
        super(text);
        this.fullText = text != null ? text : "";
        this.visibleChars = this.fullText.length();
        this.normalColor = normalColor;
        this.hoverColor = hoverColor;

        setFont(font);
        setForeground(normalColor);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        initMouseListener();
    }

    public void setTypewriterEnabled(boolean enabled) {
        this.typewriterEnabled = enabled;
    }

    private void initMouseListener() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                setForeground(hoverColor);
                if (typewriterEnabled && fullText.length() > 1) {
                    startTypewriter();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                setForeground(normalColor);
                stopTypewriterAndReset();
            }
        });
    }

    private void startTypewriter() {
        if (typewriterTimer != null && typewriterTimer.isRunning()) {
            typewriterTimer.stop();
        }
        visibleChars = 1;
        repaint();

        // 25ms per char: smooth, snappy, non-intrusive
        typewriterTimer = new Timer(25, ev -> {
            if (visibleChars < fullText.length()) {
                visibleChars++;
                repaint();
            } else {
                ((Timer) ev.getSource()).stop();
                repaint();
            }
        });
        typewriterTimer.setRepeats(true);
        typewriterTimer.start();
    }

    private void stopTypewriterAndReset() {
        if (typewriterTimer != null) {
            typewriterTimer.stop();
        }
        visibleChars = fullText.length();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        // Enforce size calculated on full text to completely eliminate layout shifts
        FontMetrics fm = getFontMetrics(getFont());
        int width = fm.stringWidth(fullText) + getInsets().left + getInsets().right + 10;
        int height = fm.getHeight() + getInsets().top + getInsets().bottom;
        return new Dimension(width, height);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        FontMetrics fm = g2.getFontMetrics(getFont());
        int x = getInsets().left;
        int y = getInsets().top + fm.getAscent();

        g2.setFont(getFont());
        g2.setColor(getForeground());

        if (visibleChars > 0 && visibleChars <= fullText.length()) {
            String visiblePart = fullText.substring(0, visibleChars);
            g2.drawString(visiblePart, x, y);

            // Subtle typewriter cursor indicator while active
            if (isHovered && visibleChars < fullText.length()) {
                int cursorX = x + fm.stringWidth(visiblePart) + 2;
                g2.setColor(UITheme.COLOR_SOFT_BLUE);
                g2.fillRect(cursorX, y - fm.getAscent() + 2, 2, fm.getAscent());
            }
        }

        g2.dispose();
    }
}
