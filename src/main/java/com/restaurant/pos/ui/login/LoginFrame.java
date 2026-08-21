package com.restaurant.pos.ui.login;

import com.restaurant.pos.AppContext;
import com.restaurant.pos.exception.AccountLockedException;
import com.restaurant.pos.exception.AuthenticationException;
import com.restaurant.pos.model.Role;
import com.restaurant.pos.model.User;
import com.restaurant.pos.ui.admin.AdminMainFrame;
import com.restaurant.pos.ui.cashier.CashierMainFrame;
import com.restaurant.pos.ui.theme.AppTheme;
import com.restaurant.pos.ui.theme.Icons;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public final class LoginFrame extends JFrame {

    private static final DateTimeFormatter LOCK_TIME_FORMAT =
            DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault());

    private final AppContext context;
    private final JTextField usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JLabel errorLabel = new JLabel("", SwingConstants.CENTER);

    public LoginFrame(AppContext context) {
        super("Restaurant POS - Terminal Sign-In");
        this.context = context;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(buildContent());
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private JPanel buildContent() {
        JPanel container = new JPanel(new MigLayout("insets 24, fill", "[center]", "[center]"));
        container.setBackground(AppTheme.BACKGROUND);

        JPanel card = new JPanel(new MigLayout("insets 28 32 24 32, wrap 1, fillx, hidemode 3", "[300!, fill]"));
        card.setBackground(AppTheme.CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        JPanel brandIconBox = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        brandIconBox.setOpaque(false);
        brandIconBox.setPreferredSize(new Dimension(38, 38));
        JLabel brandIcon = new JLabel(Icons.brand(Color.WHITE, 18));
        brandIcon.setHorizontalAlignment(SwingConstants.CENTER);
        brandIconBox.add(brandIcon, BorderLayout.CENTER);

        JPanel iconCenterer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        iconCenterer.setOpaque(false);
        iconCenterer.add(brandIconBox);
        card.add(iconCenterer, "growx, gapbottom 8");

        JLabel title = new JLabel(context.config().appName(), SwingConstants.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        card.add(title, "growx, gapbottom 2");

        JLabel subtitle = new JLabel("Sign in to your account", SwingConstants.CENTER);
        subtitle.setFont(AppTheme.captionFont());
        subtitle.setForeground(AppTheme.TEXT_SECONDARY);
        card.add(subtitle, "growx, gapbottom 16");

        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        userLabel.setForeground(AppTheme.TEXT_SECONDARY);
        card.add(userLabel, "gapbottom 3");

        usernameField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        card.add(usernameField, "growx, h 36!, gapbottom 10");

        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        passLabel.setForeground(AppTheme.TEXT_SECONDARY);
        card.add(passLabel, "gapbottom 3");

        passwordField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        card.add(passwordField, "growx, h 36!");

        JCheckBox showPassword = new JCheckBox("Show password");
        showPassword.setFont(AppTheme.captionFont());
        showPassword.setForeground(AppTheme.TEXT_SECONDARY);
        showPassword.setOpaque(false);
        showPassword.setFocusPainted(false);
        showPassword.addActionListener(e ->
                passwordField.setEchoChar(showPassword.isSelected() ? (char) 0 : '\u2022'));
        card.add(showPassword, "gaptop 4, gapbottom 8");

        errorLabel.setForeground(AppTheme.DANGER);
        errorLabel.setFont(AppTheme.captionFont());
        errorLabel.setVisible(false);
        card.add(errorLabel, "growx, gapbottom 10");

        JButton loginButton = new JButton("Sign In");
        loginButton.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_BODY));
        loginButton.setIcon(Icons.key(Color.WHITE, 14));
        loginButton.setIconTextGap(8);
        loginButton.setBackground(AppTheme.PRIMARY);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        loginButton.addActionListener(e -> attemptLogin());
        card.add(loginButton, "growx, h 40!");

        passwordField.addActionListener(e -> attemptLogin());

        container.add(card);
        return container;
    }

    private void attemptLogin() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();

        if (username.isEmpty() || password.length == 0) {
            showError("Enter your username and password.");
            return;
        }

        try {
            User user = context.authService().login(username, password);
            if (user.requiresPasswordChange()) {
                ForcePasswordChangeDialog dialog = new ForcePasswordChangeDialog(this, context, user);
                dialog.setVisible(true);
                if (!dialog.isChanged()) {
                    return;
                }
                long tempUserId = user.id();
                user = context.userService().findAll().stream().filter(u -> u.id().equals(tempUserId)).findFirst().orElse(user);
            }
            openMainFrame(user);
        } catch (AccountLockedException ex) {
            showError(lockoutMessage(ex));
        } catch (AuthenticationException ex) {
            showError(ex.getMessage());
        } finally {
            Arrays.fill(password, '\0');
            passwordField.setText("");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        pack();
    }

    private String lockoutMessage(AccountLockedException ex) {
        if (ex.lockedUntil() == null) {
            return ex.getMessage();
        }
        return "Account is locked until " + LOCK_TIME_FORMAT.format(ex.lockedUntil()) + ".";
    }

    private void openMainFrame(User user) {
        dispose();
        SwingUtilities.invokeLater(() -> {
            if (user.role() == Role.ADMINISTRATOR) {
                new AdminMainFrame(context, user).setVisible(true);
            } else {
                new CashierMainFrame(context, user).setVisible(true);
            }
        });
    }
}
