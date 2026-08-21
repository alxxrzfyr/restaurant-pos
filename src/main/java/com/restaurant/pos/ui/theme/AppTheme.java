package com.restaurant.pos.ui.theme;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.util.Map;
import javax.swing.UIManager;

public final class AppTheme {

    public static final Color BACKGROUND = Color.decode("#F8FAFC");
    public static final Color CARD = Color.decode("#FFFFFF");
    public static final Color CARD_HOVER = Color.decode("#F8FAFC");
    public static final Color PRIMARY = Color.decode("#0F172A");
    public static final Color ACCENT = Color.decode("#2563EB");
    public static final Color ACCENT_HOVER = Color.decode("#1D4ED8");
    public static final Color ACCENT_SUBTLE = Color.decode("#EFF6FF");

    public static final Color SUCCESS = Color.decode("#059669");
    public static final Color SUCCESS_BG = Color.decode("#ECFDF5");
    public static final Color SUCCESS_BORDER = Color.decode("#A7F3D0");

    public static final Color WARNING = Color.decode("#D97706");
    public static final Color WARNING_BG = Color.decode("#FFFBEB");
    public static final Color WARNING_BORDER = Color.decode("#FDE68A");

    public static final Color DANGER = Color.decode("#E11D48");
    public static final Color DANGER_BG = Color.decode("#FFF1F2");
    public static final Color DANGER_BORDER = Color.decode("#FECDD3");

    public static final Color BORDER = Color.decode("#E2E8F0");
    public static final Color BORDER_SUBTLE = Color.decode("#F1F5F9");
    public static final Color TEXT_PRIMARY = Color.decode("#0F172A");
    public static final Color TEXT_SECONDARY = Color.decode("#64748B");
    public static final Color TEXT_MUTED = Color.decode("#94A3B8");

    public static final int FONT_SIZE_DASHBOARD_TITLE = 22;
    public static final int FONT_SIZE_PAGE_TITLE = 18;
    public static final int FONT_SIZE_SECTION_HEADER = 15;
    public static final int FONT_SIZE_TABLE_HEADER = 13;
    public static final int FONT_SIZE_BODY = 13;
    public static final int FONT_SIZE_CAPTION = 12;
    public static final int FONT_SIZE_BADGE = 11;

    private static final String FONT_FAMILY = Font.SANS_SERIF;

    private AppTheme() {
    }

    public static void install() {
        FlatLaf.setGlobalExtraDefaults(Map.of(
                "@background", "#F8FAFC",
                "@accentColor", "#2563EB"));
        FlatLightLaf.setup();

        UIManager.put("Component.arc", 8);
        UIManager.put("Button.arc", 8);
        UIManager.put("Button.margin", new Insets(8, 16, 8, 16));
        UIManager.put("Button.iconTextGap", 8);
        UIManager.put("Button.background", CARD);
        UIManager.put("Button.foreground", TEXT_PRIMARY);
        UIManager.put("Button.borderColor", BORDER);
        UIManager.put("Button.hoverBorderColor", BORDER);
        UIManager.put("Button.hoverBackground", CARD);
        UIManager.put("Button.default.background", PRIMARY);
        UIManager.put("Button.default.foreground", Color.WHITE);
        UIManager.put("Button.default.hoverBackground", PRIMARY);
        UIManager.put("Button.default.borderColor", PRIMARY);
        UIManager.put("Button.default.hoverBorderColor", PRIMARY);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("TextComponent.margin", new Insets(6, 12, 6, 12));
        UIManager.put("ProgressBar.arc", 8);
        UIManager.put("CheckBox.arc", 4);
        UIManager.put("ScrollBar.thumbArc", 6);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("TabbedPane.tabArc", 8);
        UIManager.put("TabbedPane.showTabSeparators", false);
        UIManager.put("TabbedPane.underlineColor", ACCENT);
        UIManager.put("TabbedPane.tabInsets", new Insets(10, 20, 10, 20));
        UIManager.put("Table.selectionBackground", Color.decode("#EFF6FF"));
        UIManager.put("Table.selectionForeground", Color.decode("#1E40AF"));
        UIManager.put("Table.gridColor", Color.decode("#F1F5F9"));
        UIManager.put("Table.rowHeight", 38);
        UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
        UIManager.put("TableHeader.bottomSeparatorColor", Color.decode("#E2E8F0"));
        UIManager.put("TableHeader.height", 40);
        UIManager.put("defaultFont", bodyFont());
    }

    public static Font titleFont(int size) {
        return new Font(FONT_FAMILY, Font.BOLD, size);
    }

    public static Font sectionFont(int size) {
        return new Font(FONT_FAMILY, Font.BOLD, size);
    }

    public static Font mediumFont(int size) {
        return new Font(FONT_FAMILY, Font.PLAIN, size);
    }

    public static Font bodyFont() {
        return new Font(FONT_FAMILY, Font.PLAIN, FONT_SIZE_BODY);
    }

    public static Font boldBodyFont() {
        return new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_BODY);
    }

    public static Font captionFont() {
        return new Font(FONT_FAMILY, Font.PLAIN, FONT_SIZE_CAPTION);
    }

    public static Font badgeFont() {
        return new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_BADGE);
    }

    public static Font monoFont(int size) {
        return new Font(Font.MONOSPACED, Font.PLAIN, size);
    }
}
