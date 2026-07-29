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

    private static final int    HEIGHT      = 44;
    private static final Color  ACTIVE_BG   = new Color(239, 246, 255);
    private static final Color  HOVER_BG    = new Color(248, 250, 252);
    private static final Color  TEXT_NORMAL = new Color(71, 85, 105);
    private static final int    INDICATOR   = 3;

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
        setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
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
            setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
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

        if (!isEnabled()) {
            g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.4f));
        }

        if (isSelected()) {

            g2.setColor(ACTIVE_BG);
            g2.fillRect(0, 0, w, h);
            g2.setColor(AppTheme.PRIMARY);
            g2.fillRect(0, 0, INDICATOR, h);
            setForeground(AppTheme.PRIMARY);
            setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
            setIcon(activeIcon);
        } else if (getModel().isRollover()) {

            g2.setColor(HOVER_BG);
            g2.fillRect(0, 0, w, h);
            setForeground(AppTheme.TEXT_PRIMARY);
            setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
            setIcon(inactiveIcon);
        } else {

            setForeground(TEXT_NORMAL);
            setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
            setIcon(inactiveIcon);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
