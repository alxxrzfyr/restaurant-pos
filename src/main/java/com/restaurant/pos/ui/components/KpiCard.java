package com.restaurant.pos.ui.components;

import com.restaurant.pos.ui.theme.AppTheme;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public final class KpiCard extends JPanel {

    private static final int ACCENT_WIDTH = 4;

    private final JLabel valueLabel = new JLabel("0");
    private final JLabel changeLabel = new JLabel(" ");
    private final Color accentColor;

    public KpiCard(String label) {
        this(label, AppTheme.PRIMARY);
    }

    public KpiCard(String label, Color accentColor) {
        this(label, accentColor, null);
    }

    public KpiCard(String label, Color accentColor, Icon icon) {
        super(new BorderLayout(0, 4));
        this.accentColor = accentColor != null ? accentColor : AppTheme.PRIMARY;

        setBackground(AppTheme.CARD);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER),
                BorderFactory.createEmptyBorder(10, 14, 10, 12)));
        setPreferredSize(new Dimension(0, 96));
        setMinimumSize(new Dimension(140, 90));

        JPanel topRow = new JPanel(new BorderLayout(6, 0));
        topRow.setOpaque(false);

        JLabel titleLabel = new JLabel(label.toUpperCase());
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        titleLabel.setForeground(AppTheme.TEXT_SECONDARY);
        topRow.add(titleLabel, BorderLayout.CENTER);

        if (icon != null) {
            JLabel iconLabel = new JLabel(icon);
            topRow.add(iconLabel, BorderLayout.EAST);
        }

        add(topRow, BorderLayout.NORTH);

        valueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        valueLabel.setForeground(AppTheme.TEXT_PRIMARY);
        add(valueLabel, BorderLayout.CENTER);

        changeLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        changeLabel.setForeground(AppTheme.TEXT_SECONDARY);
        add(changeLabel, BorderLayout.SOUTH);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
        if (value != null && value.length() > 16) {
            valueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        } else if (value != null && value.length() > 12) {
            valueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        } else {
            valueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        }
    }

    public void setChangeText(String text, boolean positive) {
        setChangeText(text, positive ? AppTheme.SUCCESS : AppTheme.DANGER);
    }

    public void setChangeText(String text, Color color) {
        changeLabel.setText(text);
        changeLabel.setForeground(color);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(accentColor);
        g2.fillRect(0, 0, ACCENT_WIDTH, getHeight());
        g2.dispose();
    }
}
