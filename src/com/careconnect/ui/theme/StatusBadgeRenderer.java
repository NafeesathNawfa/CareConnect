package com.careconnect.ui.theme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Table cell renderer displaying status values (AVAILABLE, BOOKED, COMPLETED, CANCELLED, BLOCKED, WAITING)
 * as clean, restrained pill badges adhering to the design palette.
 */
public class StatusBadgeRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        String status = value != null ? value.toString() : "";
        StatusBadgePanel badge = new StatusBadgePanel(status);
        if (isSelected) {
            badge.setContainerBackground(table.getSelectionBackground());
        } else {
            badge.setContainerBackground(row % 2 == 0 ? UITheme.COLOR_SURFACE : UITheme.COLOR_SURFACE_ALT);
        }
        return badge;
    }

    public static class StatusBadgePanel extends JPanel {
        private final String text;
        private Color containerBg = Color.WHITE;
        private Color badgeBg = Color.LIGHT_GRAY;
        private Color badgeFg = Color.BLACK;

        public StatusBadgePanel(String text) {
            this.text = text != null ? text.toUpperCase() : "";
            setLayout(new FlowLayout(FlowLayout.CENTER, 0, 8));
            setOpaque(true);

            switch (this.text) {
                case "AVAILABLE":
                case "OPEN":
                case "COMPLETED":
                    badgeBg = Color.decode("#E8F5E9");
                    badgeFg = Color.decode("#1B5E20");
                    break;
                case "BOOKED":
                    badgeBg = Color.decode("#E1ECF4");
                    badgeFg = UITheme.COLOR_DEEP_BLUE;
                    break;
                case "CANCELLED":
                case "CLOSED":
                    badgeBg = Color.decode("#FDEDEC");
                    badgeFg = Color.decode("#B71C1C");
                    break;
                case "BLOCKED":
                case "NO_SHOW":
                case "WAITING":
                    badgeBg = Color.decode("#FEF3C7");
                    badgeFg = Color.decode("#B45309");
                    break;
                default:
                    badgeBg = Color.decode("#ECEFF1");
                    badgeFg = Color.decode("#37474F");
                    break;
            }
        }

        public void setContainerBackground(Color bg) {
            this.containerBg = bg;
            setBackground(bg);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Font font = UITheme.FONT_SMALL;
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();

            int textWidth = fm.stringWidth(text);
            int badgeWidth = textWidth + 16;
            int badgeHeight = 22;

            int x = (getWidth() - badgeWidth) / 2;
            int y = (getHeight() - badgeHeight) / 2;

            // Fill pill
            g2.setColor(badgeBg);
            g2.fillRoundRect(x, y, badgeWidth, badgeHeight, 6, 6);

            // Draw text
            g2.setColor(badgeFg);
            int textX = x + (badgeWidth - textWidth) / 2;
            int textY = y + (badgeHeight + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(text, textX, textY);

            g2.dispose();
        }
    }
}
