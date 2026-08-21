package com.restaurant.pos.ui.admin;

import com.restaurant.pos.AppContext;
import com.restaurant.pos.model.User;
import com.restaurant.pos.ui.components.Sidebar;
import com.restaurant.pos.ui.login.LoginFrame;
import com.restaurant.pos.ui.login.PasswordLogoutDialog;
import com.restaurant.pos.ui.order.OrderHistoryPanel;
import com.restaurant.pos.ui.theme.AppTheme;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public final class AdminMainFrame extends JFrame {

    private static final String CARD_DASHBOARD = "DASHBOARD";
    private static final String CARD_ORDER_HISTORY = "ORDER_HISTORY";
    private static final String CARD_MENU = "MENU";
    private static final String CARD_REPORTS = "REPORTS";
    private static final String CARD_SETTINGS = "SETTINGS";
    private static final String CARD_USERS = "USERS";

    private final AppContext context;
    private final User currentUser;

    public AdminMainFrame(AppContext context, User currentUser) {
        super("Restaurant POS - Admin - " + currentUser.displayName());
        this.context = context;
        this.currentUser = currentUser;

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                JOptionPane.showMessageDialog(AdminMainFrame.this,
                        "Window closing via titlebar 'X' is disabled.\nPlease use the Log Out button in the sidebar to exit.",
                        "Logout Required", JOptionPane.WARNING_MESSAGE);
            }
        });

        setContentPane(buildContent());
        setSize(1663, 836);
        setMinimumSize(new java.awt.Dimension(1280, 768));
        setLocationRelativeTo(null);
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(AppTheme.BACKGROUND);

        JPanel cards = new JPanel(new CardLayout());
        cards.setBackground(AppTheme.BACKGROUND);
        cards.add(new DashboardPanel(context, currentUser), CARD_DASHBOARD);
        cards.add(new OrderHistoryPanel(context, currentUser), CARD_ORDER_HISTORY);
        cards.add(new MenuManagementPanel(context, currentUser), CARD_MENU);
        cards.add(new ReportsPanel(context), CARD_REPORTS);
        cards.add(new SettingsPanel(context, currentUser), CARD_SETTINGS);
        cards.add(new UserManagementPanel(context, currentUser), CARD_USERS);

        Sidebar sidebar = new Sidebar(cards, (CardLayout) cards.getLayout());
        sidebar.setCurrentUser(currentUser);
        sidebar.addProfileCard(currentUser);
        sidebar.addSection("MAIN");
        sidebar.addItem(CARD_DASHBOARD,     "Dashboard");
        sidebar.addItem(CARD_ORDER_HISTORY, "Order History");
        sidebar.addItem(CARD_MENU,          "Menu");
        sidebar.addItem(CARD_REPORTS,       "Reports");
        sidebar.addSection("SYSTEM");
        sidebar.addItem(CARD_SETTINGS,      "Settings");
        sidebar.addItem(CARD_USERS,         "Users");
        sidebar.selectFirst();

        sidebar.setLogoutListener(e -> performPasswordProtectedLogout());

        setupKeyboardShortcuts(cards, (CardLayout) cards.getLayout());

        content.add(sidebar, BorderLayout.WEST);
        content.add(cards, BorderLayout.CENTER);
        return content;
    }

    private void setupKeyboardShortcuts(JPanel cards, CardLayout cardLayout) {
        JComponent root = getRootPane();
        var inputMap = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        var actionMap = root.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "NAV_DASHBOARD");
        actionMap.put("NAV_DASHBOARD", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { cardLayout.show(cards, CARD_DASHBOARD); }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "OPEN_SEARCH");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK), "OPEN_SEARCH");
        actionMap.put("OPEN_SEARCH", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                new com.restaurant.pos.ui.order.SearchDialog(AdminMainFrame.this, context).setVisible(true);
            }
        });
    }

    private void performPasswordProtectedLogout() {
        PasswordLogoutDialog dialog = new PasswordLogoutDialog(this, context, currentUser);
        dialog.setVisible(true);
        if (dialog.isAuthenticated()) {
            context.authService().logout(currentUser);
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame(context).setVisible(true));
        }
    }
}
