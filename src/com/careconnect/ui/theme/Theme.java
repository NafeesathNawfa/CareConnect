package com.careconnect.ui.theme;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;

/**
 * CareConnect login redesign design system.
 * Centralizes every color, font, radius and spacing value used by the new
 * login experience so the same tokens can be reused on dashboards and sidebars.
 *
 * Tokens follow the agreed spec:
 *   background #F7F9FB, cards white 16px radius,
 *   primary navy #003A63, secondary #3B5F8F, tint #DCEBFA,
 *   input fill #F3F5F7 / border #E2E8F0,
 *   headings navy, body slate #475569, hints #64748B.
 */
public final class Theme {

    private Theme() {}

    // ---- Surfaces ------------------------------------------------------
    public static final Color BG                = Color.decode("#F7F9FB");
    public static final Color CARD              = Color.decode("#FFFFFF");
    public static final Color CARD_BORDER       = Color.decode("#E2E8F0");
    public static final Color SHADOW            = new Color(15, 23, 42, 26);
    public static final Color HERO_TOP          = Color.decode("#EAF3FC");
    public static final Color HERO_BOTTOM       = Color.decode("#F7FAFE");

    // ---- Brand ---------------------------------------------------------
    public static final Color PRIMARY           = Color.decode("#003A63");
    public static final Color PRIMARY_HOVER     = Color.decode("#002B4A");
    public static final Color PRIMARY_PRESSED   = Color.decode("#001F36");
    public static final Color SECONDARY         = Color.decode("#3B5F8F");
    public static final Color SECONDARY_HOVER   = Color.decode("#325079");
    public static final Color TINT              = Color.decode("#DCEBFA");
    public static final Color TINT_BORDER       = Color.decode("#C3DDF5");

    // ---- Inputs --------------------------------------------------------
    public static final Color INPUT_FILL        = Color.decode("#F3F5F7");
    public static final Color INPUT_BORDER      = Color.decode("#E2E8F0");
    public static final Color INPUT_BORDER_FOCUS= PRIMARY;

    // ---- Text ----------------------------------------------------------
    public static final Color TEXT_HEADING      = PRIMARY;
    public static final Color TEXT_BODY         = Color.decode("#475569");
    public static final Color TEXT_HINT         = Color.decode("#64748B");
    public static final Color TEXT_ON_PRIMARY   = Color.decode("#FFFFFF");
    public static final Color TEXT_ON_TINT      = Color.decode("#0C4A73");

    // ---- Status --------------------------------------------------------
    public static final Color SUCCESS           = Color.decode("#14804A");
    public static final Color SUCCESS_BG        = Color.decode("#E7F6EE");
    public static final Color WARNING           = Color.decode("#B45309");
    public static final Color WARNING_BG        = Color.decode("#FEF3E2");
    public static final Color DANGER            = Color.decode("#C0392B");
    public static final Color DANGER_BG         = Color.decode("#FCECEA");
    public static final Color INFO_BG           = Color.decode("#EAF3FC");
    public static final Color NEUTRAL_CHIP_BG   = Color.decode("#EEF2F6");

    // ---- Radii ---------------------------------------------------------
    public static final int RADIUS_CARD         = 16;
    public static final int RADIUS_CONTROL      = 10;
    public static final int RADIUS_PILL         = 999;

    // ---- Spacing -------------------------------------------------------
    public static final int SPACE_SM            = 8;
    public static final int SPACE_MD            = 16;
    public static final int SPACE_LG            = 24;
    public static final int SPACE_XL            = 32;

    // ---- Sizing --------------------------------------------------------
    public static final int FIELD_HEIGHT        = 48;
    public static final int BUTTON_HEIGHT       = 48;

    // ---- Fonts ---------------------------------------------------------
    // Prefer Inter / Plus Jakarta Sans if present, else Segoe UI, else SansSerif.
    public static final Font FONT_DISPLAY = resolve("Inter", "Plus Jakarta Sans", "Segoe UI", Font.BOLD, 40);
    public static final Font FONT_TITLE   = resolve("Inter", "Plus Jakarta Sans", "Segoe UI", Font.BOLD, 30);
    public static final Font FONT_HEADING = resolve("Inter", "Plus Jakarta Sans", "Segoe UI", Font.BOLD, 22);
    public static final Font FONT_LABEL   = resolve("Inter", "Plus Jakarta Sans", "Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BODY    = resolve("Inter", "Plus Jakarta Sans", "Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL   = resolve("Inter", "Plus Jakarta Sans", "Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_MONO    = new Font("Consolas", Font.PLAIN, 12);

    private static Font resolve(String first, String second, String third, int style, int size) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] names = ge.getAvailableFontFamilyNames();
        for (String want : new String[]{first, second, third}) {
            for (String have : names) {
                if (have.equalsIgnoreCase(want)) return new Font(have, style, size);
            }
        }
        return new Font("SansSerif", style, size);
    }

    /** A soft, layered drop shadow used by {@link com.careconnect.ui.components.RoundedPanel}. */
    public static Color shadowAt(float alpha) {
        return new Color(15, 23, 42, Math.max(0, Math.min(255, (int) (alpha * 255))));
    }
}
