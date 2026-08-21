package com.restaurant.pos.ui.components;

import com.restaurant.pos.ui.theme.AppTheme;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public final class SecondaryButton extends JButton {

    private int arc = 8;
    private Color bgColor = AppTheme.CARD;
    private Color borderColor = AppTheme.BORDER;
    private Color fgColor = AppTheme.TEXT_PRIMARY;

    @SuppressWarnings("this-escape")
    public SecondaryButton(String text) {
        this(text, null);
    }

    @SuppressWarnings("this-escape")
    public SecondaryButton(String text, Icon icon) {
        super(text);
        if (icon != null) {
            setIcon(icon);
            setIconTextGap(8);
        }
        setFont(AppTheme.bodyFont());
        setForeground(fgColor);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setContentAreaFilled(false);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
    }

    public void setArc(int arc) {
        this.arc = arc;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, w, h, arc, arc);

        g2.setColor(borderColor);
        g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

        g2.dispose();
        super.paintComponent(g);
    }
}
