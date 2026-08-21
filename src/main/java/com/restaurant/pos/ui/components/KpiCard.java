package com.restaurant.pos.ui.components;

import com.restaurant.pos.ui.theme.AppTheme;
import net.miginfocom.swing.MigLayout;

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
import java.awt.geom.RoundRectangle2D;

public final class KpiCard extends JPanel {

    private final JLabel titleLabel = new JLabel();
    private final JLabel valueLabel = new JLabel("0");
    private final JLabel changeLabel = new JLabel(" ");
    private final JLabel iconLabel = new JLabel();
    private final JPanel iconBadge;
    private final JPanel trendPill;

    public KpiCard(String label) {
        this(label, null, null);
    }

    public KpiCard(String label, Color accentColor) {
        this(label, accentColor, null);
    }

    public KpiCard(String label, Color accentColor, Icon icon) {
        super(new MigLayout("insets 12 14 12 14, fillx, wrap 1", "[grow, fill]", "[]4[]4[]"));

        setBackground(AppTheme.CARD);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        setPreferredSize(new Dimension(160, 114));
        setMinimumSize(new Dimension(130, 108));

        JPanel topRow = new JPanel(new MigLayout("insets 0, fillx", "[grow][]"));
        topRow.setOpaque(false);

        titleLabel.setText(label != null ? label.toUpperCase() : "");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        titleLabel.setForeground(AppTheme.TEXT_SECONDARY);
        topRow.add(titleLabel, "growx, aligny center");

        iconBadge = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.decode("#F1F5F9"));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconBadge.setOpaque(false);
        iconBadge.setPreferredSize(new Dimension(26, 26));
        iconBadge.add(iconLabel, BorderLayout.CENTER);
        iconLabel.setHorizontalAlignment(JLabel.CENTER);

        if (icon != null) {
            iconLabel.setIcon(icon);
            topRow.add(iconBadge, "w 26!, h 26!");
        }

        add(topRow, "growx");

        valueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 21));
        valueLabel.setForeground(AppTheme.TEXT_PRIMARY);
        add(valueLabel, "growx");

        trendPill = new JPanel(new BorderLayout());
        trendPill.setOpaque(false);
        changeLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        changeLabel.setForeground(AppTheme.TEXT_SECONDARY);
        trendPill.add(changeLabel, BorderLayout.WEST);

        add(trendPill, "growx");
    }

    public void setIcon(Icon icon) {
        iconLabel.setIcon(icon);
        iconBadge.setVisible(icon != null);
    }

    public void setValue(String value) {
        valueLabel.setText(value != null ? value : "0");
        if (value == null) {
            valueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 21));
            return;
        }

        int len = value.length();
        if (len > 18) {
            valueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        } else if (len > 14) {
            valueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        } else if (len > 10) {
            valueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        } else {
            valueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 21));
        }
    }

    public void setChangeText(String text, boolean positive) {
        setChangeText(text, positive ? AppTheme.SUCCESS : AppTheme.DANGER);
    }

    public void setChangeText(String text, Color color) {
        changeLabel.setText(text != null && !text.isBlank() ? text : " ");
        changeLabel.setForeground(color != null ? color : AppTheme.TEXT_SECONDARY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
        g2.dispose();
        super.paintComponent(g);
    }
}
