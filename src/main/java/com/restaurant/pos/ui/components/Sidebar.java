package com.restaurant.pos.ui.components;

import com.restaurant.pos.model.Role;
import com.restaurant.pos.model.User;
import com.restaurant.pos.ui.theme.AppTheme;
import com.restaurant.pos.ui.theme.Icons;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Sidebar extends JPanel {

    public static final int WIDTH = 224;

    private final Container contentContainer;
    private final CardLayout cardLayout;
    private final ButtonGroup navGroup = new ButtonGroup();
    private final Map<String, NavButton> buttonsByKey = new LinkedHashMap<>();

    private final JPanel topContainer = new JPanel(new MigLayout("insets 0, wrap 1, fillx", "[grow, fill]"));
    private final JPanel navItemsPanel = new JPanel(new MigLayout("insets 8 0 8 0, wrap 1, fillx, gapy 2", "[grow, fill]"));
    private final LogoutNavButton logoutButton = new LogoutNavButton("Log Out");

    private User currentUser;
    private ProfilePhotoPanel photoPanel;
    private ActionListener photoSaveListener;
    private JPanel profileCard;
    private boolean collapsed = false;
    private boolean firstSectionAdded = false;

    public Sidebar(Container contentContainer, CardLayout cardLayout) {
        super(new BorderLayout(0, 0));
        this.contentContainer = contentContainer;
        this.cardLayout = cardLayout;

        setBackground(AppTheme.CARD);
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, AppTheme.BORDER));
        setPreferredSize(new Dimension(WIDTH, 0));

        topContainer.setOpaque(false);
        topContainer.add(buildHeader(), "growx");
        add(topContainer, BorderLayout.NORTH);

        navItemsPanel.setOpaque(false);
        add(navItemsPanel, BorderLayout.CENTER);

        add(buildFooter(), BorderLayout.SOUTH);
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void setPhotoSaveListener(ActionListener listener) {
        this.photoSaveListener = listener;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new MigLayout("insets 14 16 12 16, fillx, wrap 1", "[grow, fill]"));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER));

        JPanel brand = new JPanel(new MigLayout("insets 0", "[]10[]"));
        brand.setOpaque(false);


        JLabel title = new JLabel("RESTAURANT POS");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        title.setForeground(new Color(30, 41, 59));
        brand.add(title);

        header.add(brand, "growx");
        return header;
    }

    public JPanel buildProfileCard(User user) {
        this.currentUser = user;

        if (profileCard != null) {
            topContainer.remove(profileCard);
        }

        profileCard = new JPanel(new MigLayout("insets 12 16 12 16, fillx", "[]12[grow]"));
        profileCard.setOpaque(false);
        profileCard.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER));

        photoPanel = new ProfilePhotoPanel(user.photoPath(), user.displayName(), false);
        profileCard.add(photoPanel, "w 44!, h 44!");

        JPanel info = new JPanel(new MigLayout("insets 0, wrap 1, gapy 2"));
        info.setOpaque(false);

        JLabel nameLabel = new JLabel(user.displayName());
        nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        nameLabel.setForeground(AppTheme.TEXT_PRIMARY);

        String roleText = formatRoleName(user);
        JLabel roleLabel = new JLabel(roleText);
        roleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        roleLabel.setForeground(AppTheme.TEXT_SECONDARY);

        info.add(nameLabel);
        info.add(roleLabel);
        profileCard.add(info, "growx");

        topContainer.add(profileCard, "growx");
        topContainer.revalidate();
        topContainer.repaint();

        return profileCard;
    }

    public void addProfileCard(User user) {
        buildProfileCard(user);
    }

    public void addSection(String title) {
        if (firstSectionAdded) {
            JPanel divider = new JPanel();
            divider.setBackground(new Color(226, 232, 240));
            navItemsPanel.add(divider, "growx, h 1!, gapleft 16, gapright 16, gaptop 12, gapbottom 6");
        }
        firstSectionAdded = true;

        JLabel sectionLabel = new JLabel(title.toUpperCase());
        sectionLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        sectionLabel.setForeground(new Color(148, 163, 184));
        sectionLabel.setBorder(BorderFactory.createEmptyBorder(6, 16, 4, 16));
        navItemsPanel.add(sectionLabel, "growx");
    }

    public NavButton addItem(String key, String label) {
        Icon inactiveIcon = resolveIcon(key, false);
        Icon activeIcon   = resolveIcon(key, true);
        NavButton button  = new NavButton(label, inactiveIcon, activeIcon);
        button.setCollapsed(collapsed);
        button.addActionListener(e -> cardLayout.show(contentContainer, key));
        navGroup.add(button);
        buttonsByKey.put(key, button);
        navItemsPanel.add(button, "growx");
        return button;
    }

    public void selectFirst() {
        buttonsByKey.values().stream().findFirst().ifPresent(button -> {
            button.setSelected(true);
            cardLayout.show(contentContainer, buttonsByKey.entrySet().iterator().next().getKey());
        });
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new MigLayout("insets 8 0 8 0, fillx"));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.BORDER));

        footer.add(logoutButton, "growx, h 44!");
        return footer;
    }

    public void setLogoutListener(ActionListener listener) {
        logoutButton.addActionListener(listener);
    }

    public String getCurrentPhotoPath() {
        return photoPanel != null ? photoPanel.getCurrentPhotoPath() : null;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
        setPreferredSize(new Dimension(collapsed ? 64 : WIDTH, 0));
        buttonsByKey.values().forEach(btn -> btn.setCollapsed(collapsed));
        logoutButton.setCollapsed(collapsed);
        revalidate();
        repaint();
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    private String formatRoleName(User user) {
        if (user == null || user.role() == null) return "";
        if (user.role() == Role.ADMINISTRATOR) {
            return "System Administrator";
        } else if (user.role() == Role.CASHIER) {
            return "Cashier";
        }
        String name = user.role().name();
        return name.charAt(0) + name.substring(1).toLowerCase();
    }

    private Icon resolveIcon(String key, boolean active) {
        int size  = 20;
        Color col = active ? AppTheme.PRIMARY : AppTheme.TEXT_SECONDARY;
        return switch (key) {
            case "DASHBOARD"                    -> Icons.dashboard(col, size);
            case "NEW_ORDER"                    -> Icons.orders(col, size);
            case "ORDER_HISTORY"                -> Icons.orders(col, size);
            case "MENU", "MENU_LOOKUP"          -> Icons.menu(col, size);
            case "REPORTS"                      -> Icons.reports(col, size);
            case "SETTINGS"                     -> Icons.settings(col, size);
            case "USERS"                        -> Icons.users(col, size);
            default                             -> Icons.menu(col, size);
        };
    }

    private static final class LogoutNavButton extends JButton {
        private static final Color HOVER_BG  = new Color(254, 242, 242);
        private static final Color ACTIVE_BG = new Color(254, 226, 226);
        private static final Color TEXT_NORMAL = new Color(71, 85, 105);

        private final Icon inactiveIcon;
        private final Icon activeIcon;
        private final String labelText;

        public LogoutNavButton(String label) {
            super(label);
            this.labelText    = label;
            this.inactiveIcon = Icons.logout(TEXT_NORMAL, 20);
            this.activeIcon   = Icons.logout(AppTheme.DANGER, 20);

            setIcon(inactiveIcon);
            setIconTextGap(12);
            setHorizontalAlignment(SwingConstants.LEFT);
            setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(WIDTH, 44));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
            setContentAreaFilled(false);
        }

        public void setCollapsed(boolean collapsed) {
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

            if (getModel().isPressed()) {
                g2.setColor(ACTIVE_BG);
                g2.fillRect(0, 0, w, h);
                setForeground(AppTheme.DANGER);
                setIcon(activeIcon);
            } else if (getModel().isRollover()) {
                g2.setColor(HOVER_BG);
                g2.fillRect(0, 0, w, h);
                setForeground(AppTheme.DANGER);
                setIcon(activeIcon);
            } else {
                setForeground(TEXT_NORMAL);
                setIcon(inactiveIcon);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
