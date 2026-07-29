package com.restaurant.pos.ui.components;

import com.restaurant.pos.ui.theme.AppTheme;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;

public final class StripedTableCellRenderer extends DefaultTableCellRenderer {

    private static final Color ROW_EVEN = AppTheme.CARD;
    private static final Color ROW_ODD = new Color(248, 250, 252);
    private static final Color SELECTION_BG = new Color(219, 234, 254);
    private static final Color SELECTION_FG = Color.decode("#1E40AF");

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                    boolean isSelected, boolean hasFocus,
                                                    int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 8));

        if (isSelected) {
            setBackground(SELECTION_BG);
            setForeground(SELECTION_FG);
        } else {
            setBackground(row % 2 == 0 ? ROW_EVEN : ROW_ODD);
            setForeground(AppTheme.TEXT_PRIMARY);

            String text = value != null ? value.toString() : "";
            if ("PAID".equalsIgnoreCase(text) || "Available".equalsIgnoreCase(text) || "Yes".equalsIgnoreCase(text)) {
                setForeground(AppTheme.SUCCESS);
            } else if ("VOIDED".equalsIgnoreCase(text) || "86'd".equalsIgnoreCase(text) || "No".equalsIgnoreCase(text)) {
                setForeground(AppTheme.DANGER);
            } else if ("OPEN".equalsIgnoreCase(text)) {
                setForeground(AppTheme.WARNING);
            }
        }

        return this;
    }

    public static void apply(JTable table) {
        StripedTableCellRenderer renderer = new StripedTableCellRenderer();
        for (int col = 0; col < table.getColumnCount(); col++) {
            table.getColumnModel().getColumn(col).setCellRenderer(renderer);
        }
        table.setShowGrid(false);
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));
    }
}
