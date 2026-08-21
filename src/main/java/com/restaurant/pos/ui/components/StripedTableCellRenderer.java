package com.restaurant.pos.ui.components;

import com.restaurant.pos.ui.theme.AppTheme;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.Color;
import java.awt.Component;

public final class StripedTableCellRenderer extends DefaultTableCellRenderer {

    private static final Color ROW_EVEN = AppTheme.CARD;
    private static final Color ROW_ODD = Color.decode("#F8FAFC");
    private static final Color SELECTION_BG = Color.decode("#EFF6FF");
    private static final Color SELECTION_FG = Color.decode("#1E40AF");

    public StripedTableCellRenderer() {
        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                    boolean isSelected, boolean hasFocus,
                                                    int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);
        setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        if (isSelected) {
            setBackground(SELECTION_BG);
            setForeground(SELECTION_FG);
        } else {
            setBackground(row % 2 == 0 ? ROW_EVEN : ROW_ODD);
            setForeground(AppTheme.TEXT_PRIMARY);

            String text = value != null ? value.toString() : "";
            if ("PAID".equalsIgnoreCase(text) || "Available".equalsIgnoreCase(text) || "Yes".equalsIgnoreCase(text) || "Active".equalsIgnoreCase(text)) {
                setForeground(AppTheme.SUCCESS);
            } else if ("VOIDED".equalsIgnoreCase(text) || "86'd".equalsIgnoreCase(text) || "No".equalsIgnoreCase(text) || "Disabled".equalsIgnoreCase(text) || text.startsWith("86'd")) {
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

        TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();
        if (headerRenderer instanceof JLabel headerLabel) {
            headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        } else {
            DefaultTableCellRenderer centerHeader = new DefaultTableCellRenderer();
            centerHeader.setHorizontalAlignment(SwingConstants.CENTER);
            centerHeader.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_TABLE_HEADER));
            centerHeader.setForeground(AppTheme.TEXT_PRIMARY);
            table.getTableHeader().setDefaultRenderer(centerHeader);
        }

        table.setShowGrid(true);
        table.setGridColor(Color.decode("#F1F5F9"));
        table.setIntercellSpacing(new java.awt.Dimension(0, 1));
    }
}
