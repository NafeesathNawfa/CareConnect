package com.careconnect.ui.theme;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Central design tokens, color palette, and typography system for CareConnect.
 * Strictly adheres to the project blue palette:
 * #A4C8E1, #6FA3D8, #3B7A99, #1F4E79, #0D3B66.
 */
public class UITheme {

    // Primary Blue Palette
    public static final Color COLOR_SKY_BLUE    = Color.decode("#A4C8E1"); // Border accents & soft badges
    public static final Color COLOR_SOFT_BLUE   = Color.decode("#6FA3D8"); // Hover highlights & focus rings
    public static final Color COLOR_OCEAN_BLUE  = Color.decode("#3B7A99"); // Selected controls & secondary buttons
    public static final Color COLOR_DEEP_BLUE   = Color.decode("#1F4E79"); // Primary buttons & section headings
    public static final Color COLOR_NAVY        = Color.decode("#0D3B66"); // Sidebar & brand headers

    // Surfaces & Backgrounds
    public static final Color COLOR_BG_CANVAS   = Color.decode("#F1F5F8"); // Main window background
    public static final Color COLOR_SURFACE     = Color.decode("#FFFFFF"); // Cards, forms, tables
    public static final Color COLOR_SURFACE_ALT = Color.decode("#F8FAFC"); // Alternating table rows

    // Typography Colors
    public static final Color COLOR_TEXT_PRIMARY   = Color.decode("#172B3A"); // High contrast primary text
    public static final Color COLOR_TEXT_MUTED     = Color.decode("#5A6E7F"); // Secondary descriptions
    public static final Color COLOR_TEXT_ON_DARK   = Color.decode("#FFFFFF"); // Text on navy/deep blue

    // Semantic Status Colors
    public static final Color COLOR_SUCCESS     = Color.decode("#2E7D32");
    public static final Color COLOR_SUCCESS_BG  = Color.decode("#E8F5E9");
    public static final Color COLOR_WARNING     = Color.decode("#D97706");
    public static final Color COLOR_WARNING_BG  = Color.decode("#FEF3C7");
    public static final Color COLOR_DANGER      = Color.decode("#C0392B");
    public static final Color COLOR_DANGER_BG   = Color.decode("#FDEDEC");

    // Borders
    public static final Color COLOR_BORDER      = Color.decode("#D3DFE8");
    public static final Color COLOR_BORDER_FOCUS= COLOR_SOFT_BLUE;

    // Fonts with sensible fallbacks (Proxima Nova / Segoe UI / Arial / Lora)
    public static final Font FONT_TITLE_LARGE  = getBestFont("Segoe UI", "Proxima Nova", "Arial", Font.BOLD, 22);
    public static final Font FONT_TITLE_MEDIUM = getBestFont("Segoe UI", "Proxima Nova", "Arial", Font.BOLD, 17);
    public static final Font FONT_HEADER       = getBestFont("Segoe UI", "Proxima Nova", "Arial", Font.BOLD, 14);
    public static final Font FONT_BODY         = getBestFont("Segoe UI", "Proxima Nova", "Arial", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD    = getBestFont("Segoe UI", "Proxima Nova", "Arial", Font.BOLD, 13);
    public static final Font FONT_SMALL        = getBestFont("Segoe UI", "Proxima Nova", "Arial", Font.PLAIN, 11);
    public static final Font FONT_DESC_ITALIC  = getBestFont("Lora", "Georgia", "Segoe UI", Font.ITALIC, 13);

    private static Font getBestFont(String primary, String secondary, String fallback, int style, int size) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontNames = ge.getAvailableFontFamilyNames();
        for (String f : fontNames) {
            if (f.equalsIgnoreCase(primary)) return new Font(primary, style, size);
        }
        for (String f : fontNames) {
            if (f.equalsIgnoreCase(secondary)) return new Font(secondary, style, size);
        }
        return new Font(fallback, style, size);
    }

    public static Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, false),
                new EmptyBorder(16, 16, 16, 16)
        );
    }

    public static Border createThinBorder() {
        return new LineBorder(COLOR_BORDER, 1, false);
    }

    public static Border createFocusBorder() {
        return new LineBorder(COLOR_BORDER_FOCUS, 2, false);
    }

    public static Border createControlPadding(int top, int left, int bottom, int right) {
        return new EmptyBorder(top, left, bottom, right);
    }
}
