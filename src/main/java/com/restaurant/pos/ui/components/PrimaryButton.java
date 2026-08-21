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

public final class PrimaryButton extends JButton {

    private int arc = 8;
    private Color bgColor = AppTheme.PRIMARY;
    private Color fgColor = Color.WHITE;

    @SuppressWarnings("this-escape")
    public PrimaryButton(String text) {
        this(text, null);
    }

    @SuppressWarnings("this-escape")
    public PrimaryButton(String text, Icon icon) {
        super(text);
        if (icon != null) {
            setIcon(icon);
            setIconTextGap(8);
        }
        setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_BODY));
        setForeground(fgColor);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setContentAreaFilled(false);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
    }

    public void setArc(int arc) {
        this.arc = arc;
    }

    public void setCustomBackground(Color color) {
        this.bgColor = color;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, w, h, arc, arc);

        g2.dispose();
        super.paintComponent(g);
    }
}
