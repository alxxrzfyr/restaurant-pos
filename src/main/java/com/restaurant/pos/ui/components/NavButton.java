package com.restaurant.pos.ui.components;

import com.restaurant.pos.ui.theme.AppTheme;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public final class NavButton extends JToggleButton {

    private static final int    HEIGHT        = 42;
    private static final Color  ACTIVE_BG     = Color.decode("#0F172A");
    private static final Color  ACTIVE_FG     = Color.decode("#FFFFFF");
    private static final Color  HOVER_BG      = Color.decode("#F1F5F9");
    private static final Color  HOVER_FG      = Color.decode("#0F172A");
    private static final Color  TEXT_NORMAL   = Color.decode("#64748B");
    private static final int    RADIUS        = 8;

    private final Icon inactiveIcon;
    private final Icon activeIcon;
    private final String labelText;
    private boolean collapsed = false;

    public NavButton(String label, Icon inactiveIcon, Icon activeIcon) {
        super(label);
        this.labelText    = label;
        this.inactiveIcon = inactiveIcon;
        this.activeIcon   = activeIcon != null ? activeIcon : inactiveIcon;

        if (inactiveIcon != null) {
            setIcon(inactiveIcon);
            setIconTextGap(12);
        }
        setHorizontalAlignment(SwingConstants.LEFT);
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(224, HEIGHT));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, HEIGHT));
        setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 16));
        setContentAreaFilled(false);
    }

    public NavButton(String label, Icon icon) {
        this(label, icon, icon);
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
        if (collapsed) {
            setText("");
            setToolTipText(labelText);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        } else {
            setText(labelText);
            setToolTipText(null);
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 16));
        }
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int insetX = collapsed ? 6 : 10;
        int insetY = 2;
        int pillW = w - (insetX * 2);
        int pillH = h - (insetY * 2);

        if (!isEnabled()) {
            g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.4f));
        }

        if (isSelected()) {
            g2.setColor(ACTIVE_BG);
            g2.fillRoundRect(insetX, insetY, pillW, pillH, RADIUS, RADIUS);
            setForeground(ACTIVE_FG);
            setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
            setIcon(activeIcon);
        } else {
            setForeground(TEXT_NORMAL);
            setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
            setIcon(inactiveIcon);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
