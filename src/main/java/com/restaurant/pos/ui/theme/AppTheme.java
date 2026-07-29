package com.restaurant.pos.ui.theme;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import java.awt.Color;
import java.awt.Font;
import java.util.Map;
import javax.swing.UIManager;

public final class AppTheme {

    public static final Color BACKGROUND = Color.decode("#F8FAFC");
    public static final Color CARD = Color.decode("#FFFFFF");
    public static final Color PRIMARY = Color.decode("#2563EB");
    public static final Color SUCCESS = Color.decode("#16A34A");
    public static final Color WARNING = Color.decode("#D97706");
    public static final Color DANGER = Color.decode("#DC2626");
    public static final Color BORDER = Color.decode("#E5E7EB");
    public static final Color TEXT_PRIMARY = Color.decode("#111827");
    public static final Color TEXT_SECONDARY = Color.decode("#6B7280");

    public static final int FONT_SIZE_DASHBOARD_TITLE = 24;
    public static final int FONT_SIZE_PAGE_TITLE = 20;
    public static final int FONT_SIZE_SECTION_HEADER = 18;
    public static final int FONT_SIZE_TABLE_HEADER = 14;
    public static final int FONT_SIZE_BODY = 13;
    public static final int FONT_SIZE_CAPTION = 12;

    private static final String FONT_FAMILY = Font.SANS_SERIF;

    private AppTheme() {
    }

    public static void install() {
        FlatLaf.setGlobalExtraDefaults(Map.of(
                "@background", "#F8FAFC",
                "@accentColor", "#2563EB"));
        FlatLightLaf.setup();

        UIManager.put("Component.arc", 0);
        UIManager.put("Button.arc", 0);
        UIManager.put("TextComponent.arc", 0);
        UIManager.put("ProgressBar.arc", 0);
        UIManager.put("CheckBox.arc", 0);
        UIManager.put("defaultFont", bodyFont());
    }

    public static Font titleFont(int size) {
        return new Font(FONT_FAMILY, Font.BOLD, size);
    }

    public static Font bodyFont() {
        return new Font(FONT_FAMILY, Font.PLAIN, FONT_SIZE_BODY);
    }

    public static Font captionFont() {
        return new Font(FONT_FAMILY, Font.PLAIN, FONT_SIZE_CAPTION);
    }
}
