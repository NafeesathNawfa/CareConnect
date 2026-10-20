package com.careconnect.ui.theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import java.awt.*;

/**
 * Modern readable table component.
 * Features navy header, alternating striped rows, comfortable padding,
 * and high-contrast readable text.
 */
public class ModernTable extends JTable {

    public ModernTable() {
        super();
        initTableStyles();
    }

    public ModernTable(TableModel model) {
        super(model);
        initTableStyles();
    }

    private void initTableStyles() {
        setFont(UITheme.FONT_BODY);
        setRowHeight(38);
        setShowGrid(false);
        setShowHorizontalLines(true);
        setGridColor(Color.decode("#E5EEF5"));
        setSelectionBackground(Color.decode("#DDEAF5"));
        setSelectionForeground(UITheme.COLOR_TEXT_PRIMARY);
        setBackground(UITheme.COLOR_SURFACE);
        setFillsViewportHeight(true);

        // Header configuration
        JTableHeader header = getTableHeader();
        header.setFont(UITheme.FONT_HEADER);
        header.setBackground(UITheme.COLOR_DEEP_BLUE);
        header.setForeground(UITheme.COLOR_TEXT_ON_DARK);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        header.setReorderingAllowed(false);

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setBackground(UITheme.COLOR_DEEP_BLUE);
                l.setForeground(UITheme.COLOR_TEXT_ON_DARK);
                l.setFont(UITheme.FONT_HEADER);
                l.setBorder(new EmptyBorder(8, 12, 8, 12));
                return l;
            }
        });

        // Alternating row styling
        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? UITheme.COLOR_SURFACE : UITheme.COLOR_SURFACE_ALT);
                    c.setForeground(UITheme.COLOR_TEXT_PRIMARY);
                }
                if (c instanceof JLabel) {
                    ((JLabel) c).setBorder(new EmptyBorder(6, 12, 6, 12));
                }
                return c;
            }
        });
    }

    public static JScrollPane wrapInScrollPane(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(UITheme.createThinBorder());
        scroll.getViewport().setBackground(UITheme.COLOR_SURFACE);
        return scroll;
    }
}
