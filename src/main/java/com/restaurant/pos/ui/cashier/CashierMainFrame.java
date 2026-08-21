package com.restaurant.pos.ui.cashier;

import com.restaurant.pos.AppContext;
import com.restaurant.pos.model.User;
import com.restaurant.pos.ui.components.Sidebar;
import com.restaurant.pos.ui.login.LoginFrame;
import com.restaurant.pos.ui.order.OrderHistoryPanel;
import com.restaurant.pos.ui.order.OrderPanel;
import com.restaurant.pos.ui.theme.AppTheme;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public final class CashierMainFrame extends JFrame {

    private static final String CARD_NEW_ORDER = "NEW_ORDER";
    private static final String CARD_ORDER_HISTORY = "ORDER_HISTORY";
    private static final String CARD_MENU_LOOKUP = "MENU_LOOKUP";

    private final AppContext context;
    private final User currentUser;

    public CashierMainFrame(AppContext context, User currentUser) {
        super("Restaurant POS - Cashier - " + currentUser.displayName());
        this.context = context;
        this.currentUser = currentUser;

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                JOptionPane.showMessageDialog(CashierMainFrame.this,
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
        cards.add(new OrderPanel(context, currentUser.id(), currentUser.displayName()), CARD_NEW_ORDER);
        cards.add(new OrderHistoryPanel(context), CARD_ORDER_HISTORY);
        cards.add(new MenuLookupPanel(context), CARD_MENU_LOOKUP);

        Sidebar sidebar = new Sidebar(cards, (CardLayout) cards.getLayout());
        sidebar.setCurrentUser(currentUser);
        sidebar.addProfileCard(currentUser);
        sidebar.addSection("MAIN");
        sidebar.addItem(CARD_NEW_ORDER,     "New Order");
        sidebar.addItem(CARD_ORDER_HISTORY, "Order History");
        sidebar.addItem(CARD_MENU_LOOKUP,   "Menu Lookup");
        sidebar.selectFirst();

        sidebar.setLogoutListener(e -> performPasswordProtectedLogout());

        setupKeyboardShortcuts(cards, (CardLayout) cards.getLayout());

        content.add(sidebar, BorderLayout.WEST);
        content.add(cards, BorderLayout.CENTER);
        return content;
    }

    private void setupKeyboardShortcuts(JPanel cards, CardLayout cardLayout) {
        javax.swing.JComponent root = getRootPane();
        var inputMap = root.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
        var actionMap = root.getActionMap();

        var newOrderAction = new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { cardLayout.show(cards, CARD_NEW_ORDER); }
        };
        inputMap.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0), "NAV_NEW_ORDER");
        inputMap.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0), "NAV_NEW_ORDER");
        inputMap.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.KeyEvent.CTRL_DOWN_MASK), "NAV_NEW_ORDER");
        actionMap.put("NAV_NEW_ORDER", newOrderAction);

        var searchAction = new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                new com.restaurant.pos.ui.order.SearchDialog(CashierMainFrame.this, context).setVisible(true);
            }
        };
        inputMap.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, 0), "OPEN_SEARCH");
        inputMap.put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.KeyEvent.CTRL_DOWN_MASK), "OPEN_SEARCH");
        actionMap.put("OPEN_SEARCH", searchAction);
    }

    private void performPasswordProtectedLogout() {
        com.restaurant.pos.ui.login.PasswordLogoutDialog dialog =
                new com.restaurant.pos.ui.login.PasswordLogoutDialog(this, context, currentUser);
        dialog.setVisible(true);
        if (dialog.isAuthenticated()) {
            context.authService().logout(currentUser);
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame(context).setVisible(true));
        }
    }
}
