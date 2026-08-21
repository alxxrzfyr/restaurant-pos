package com.restaurant.pos.ui.login;

import com.restaurant.pos.AppContext;
import com.restaurant.pos.exception.AuthenticationException;
import com.restaurant.pos.model.User;
import com.restaurant.pos.ui.theme.AppTheme;
import com.restaurant.pos.ui.theme.Icons;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Frame;
import java.util.Arrays;

public final class PasswordLogoutDialog extends JDialog {

    private final AppContext context;
    private final User currentUser;

    private final JPasswordField passwordField = new JPasswordField(20);
    private final JLabel errorLabel = new JLabel(" ");

    private boolean authenticated = false;

    public PasswordLogoutDialog(Frame owner, AppContext context, User currentUser) {
        super(owner, "Confirm Logout", true);
        this.context = context;
        this.currentUser = currentUser;

        setContentPane(buildContent());
        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    private JPanel buildContent() {
        JPanel panel = new JPanel(new MigLayout("fillx, insets 24, wrap 1", "[320!]"));
        panel.setBackground(AppTheme.CARD);

        JLabel title = new JLabel("Authorize Log Out");
        title.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_SECTION_HEADER));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        panel.add(title, "gapbottom 4");

        JLabel info = new JLabel("Logged in as " + currentUser.displayName() + " (" + currentUser.username() + ")");
        info.setFont(AppTheme.bodyFont());
        info.setForeground(AppTheme.TEXT_SECONDARY);
        panel.add(info, "gapbottom 14");

        JLabel passLabel = new JLabel("Enter password to confirm:");
        passLabel.setFont(AppTheme.bodyFont());
        passLabel.setForeground(AppTheme.TEXT_PRIMARY);
        panel.add(passLabel, "gapbottom 4");

        passwordField.setFont(AppTheme.bodyFont());
        panel.add(passwordField, "growx, h 36!, gapbottom 6");

        errorLabel.setFont(AppTheme.captionFont());
        errorLabel.setForeground(AppTheme.DANGER);
        panel.add(errorLabel, "gapbottom 14");

        JPanel buttonPanel = new JPanel(new MigLayout("insets 0", "[grow]10[grow]"));
        buttonPanel.setOpaque(false);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(AppTheme.bodyFont());
        cancelBtn.setBackground(AppTheme.CARD);
        cancelBtn.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dispose());

        JButton logoutBtn = new JButton("Log Out");
        logoutBtn.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_BODY));
        logoutBtn.setIcon(Icons.logout(Color.WHITE, 16));
        logoutBtn.setIconTextGap(6);
        logoutBtn.setBackground(AppTheme.DANGER);
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> onConfirmLogout());

        buttonPanel.add(cancelBtn, "growx, h 40!");
        buttonPanel.add(logoutBtn, "growx, h 40!");

        panel.add(buttonPanel, "growx");

        passwordField.addActionListener(e -> onConfirmLogout());

        return panel;
    }

    private void onConfirmLogout() {
        char[] password = passwordField.getPassword();
        if (password.length == 0) {
            errorLabel.setText("Password cannot be empty.");
            return;
        }

        try {
            context.authService().login(currentUser.username(), password);
            authenticated = true;
            dispose();
        } catch (AuthenticationException ex) {
            errorLabel.setText("Incorrect password. Logout denied.");
        } catch (Exception ex) {
            errorLabel.setText(ex.getMessage());
        } finally {
            Arrays.fill(password, '\0');
        }
    }
}
