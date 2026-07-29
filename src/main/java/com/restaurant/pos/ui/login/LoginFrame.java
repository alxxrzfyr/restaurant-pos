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

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public final class LoginFrame extends JFrame {

    private static final DateTimeFormatter LOCK_TIME_FORMAT =
            DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault());

    private final AppContext context;
    private final JTextField usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JLabel errorLabel = new JLabel(" ", SwingConstants.CENTER);

    public LoginFrame(AppContext context) {
        super("Restaurant POS - Login");
        this.context = context;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(buildContent());
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private JPanel buildContent() {
        JPanel card = new JPanel(new MigLayout("insets 36 36 36 36, wrap 1, fillx", "[340!, fill]"));
        card.setBackground(AppTheme.CARD);


        JLabel title = new JLabel(context.config().appName(), SwingConstants.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        card.add(title, "growx, gapbottom 2");

        JLabel subtitle = new JLabel("Enterprise Terminal Sign-In", SwingConstants.CENTER);
        subtitle.setFont(AppTheme.captionFont());
        subtitle.setForeground(AppTheme.TEXT_SECONDARY);
        card.add(subtitle, "growx, gapbottom 24");

        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        userLabel.setForeground(AppTheme.TEXT_PRIMARY);
        card.add(userLabel, "gapbottom 4");

        usernameField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        card.add(usernameField, "growx, h 40!, gapbottom 14");

        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        passLabel.setForeground(AppTheme.TEXT_PRIMARY);
        card.add(passLabel, "gapbottom 4");

        passwordField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        card.add(passwordField, "growx, h 40!");

        JCheckBox showPassword = new JCheckBox("Show password");
        showPassword.setFont(AppTheme.captionFont());
        showPassword.setForeground(AppTheme.TEXT_SECONDARY);
        showPassword.setOpaque(false);
        showPassword.setFocusPainted(false);
        showPassword.addActionListener(e ->
                passwordField.setEchoChar(showPassword.isSelected() ? (char) 0 : '\u2022'));
        card.add(showPassword, "gaptop 6, gapbottom 10");

        errorLabel.setForeground(AppTheme.DANGER);
        errorLabel.setFont(AppTheme.captionFont());
        errorLabel.setPreferredSize(new Dimension(340, 20));
        card.add(errorLabel, "growx, gapbottom 14");

        JButton loginButton = new JButton("Log In");
        loginButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        loginButton.setBackground(AppTheme.PRIMARY);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(e -> attemptLogin());
        card.add(loginButton, "growx, h 42!");

        passwordField.addActionListener(e -> attemptLogin());

        return card;
    }

    private void attemptLogin() {
        errorLabel.setText(" ");
        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();

        if (username.isEmpty() || password.length == 0) {
            errorLabel.setText("Enter your username and password.");
            return;
        }

        try {
            User user = context.authService().login(username, password);
            if (user.requiresPasswordChange()) {
                ForcePasswordChangeDialog dialog = new ForcePasswordChangeDialog(this, context, user);
                dialog.setVisible(true);
                if (!dialog.isChanged()) {
                    return; // user cancelled password change
                }
                // Reload user to get updated flags
                long tempUserId = user.id();
                user = context.userService().findAll().stream().filter(u -> u.id().equals(tempUserId)).findFirst().orElse(user);
            }
            openMainFrame(user);
        } catch (AccountLockedException ex) {
            errorLabel.setText(lockoutMessage(ex));
        } catch (AuthenticationException ex) {
            errorLabel.setText(ex.getMessage());
        } finally {
            Arrays.fill(password, '\0');
            passwordField.setText("");
        }
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
