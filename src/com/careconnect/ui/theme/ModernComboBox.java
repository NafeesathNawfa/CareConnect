package com.careconnect.ui.theme;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Modern styled ComboBox honoring the blue color scheme.
 */
public class ModernComboBox<E> extends JComboBox<E> {

    public ModernComboBox() {
        super();
        initStyle();
    }

    public ModernComboBox(E[] items) {
        super(items);
        initStyle();
    }

    public ModernComboBox(ComboBoxModel<E> model) {
        super(model);
        initStyle();
    }

    private void initStyle() {
        setFont(UITheme.FONT_BODY);
        setBackground(UITheme.COLOR_SURFACE);
        setForeground(UITheme.COLOR_TEXT_PRIMARY);
        setBorder(new LineBorder(UITheme.COLOR_BORDER, 1));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, Math.max(d.height, 36));
    }
}
