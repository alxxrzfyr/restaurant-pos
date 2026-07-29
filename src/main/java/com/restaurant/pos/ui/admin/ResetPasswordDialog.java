package com.restaurant.pos.ui.admin;

import com.restaurant.pos.AppContext;
import com.restaurant.pos.model.User;
import com.restaurant.pos.ui.theme.AppTheme;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Frame;
import java.util.Arrays;

final class ResetPasswordDialog extends JDialog {

    private final AppContext context;
    private final User currentUser;
    private final User userToReset;

    private final JPasswordField passwordField = new JPasswordField(20);
    private final JPasswordField confirmPasswordField = new JPasswordField(20);
    private final JLabel errorLabel = new JLabel(" ");

    private boolean saved = false;

    ResetPasswordDialog(Frame owner, AppContext context, User currentUser, User userToReset) {
        super(owner, "Reset Password - " + userToReset.username(), true);
        this.context = context;
        this.currentUser = currentUser;
        this.userToReset = userToReset;

        setContentPane(buildContent());
        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    boolean isSaved() {
        return saved;
    }

    private JPanel buildContent() {
        JPanel panel = new JPanel(new MigLayout("fillx, insets 20, wrap 2", "[right][grow,fill]"));
        panel.setBackground(AppTheme.BACKGROUND);

        JLabel titleLabel = new JLabel("Reset Password for '" + userToReset.username() + "'");
        titleLabel.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_SECTION_HEADER));
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        panel.add(titleLabel, "span 2, gapbottom 15");

        JLabel passLabel = new JLabel("New Password:");
        passLabel.setFont(AppTheme.bodyFont());
        panel.add(passLabel);

        passwordField.setFont(AppTheme.bodyFont());
        panel.add(passwordField, "h 36!");

        JLabel confirmLabel = new JLabel("Confirm Password:");
        confirmLabel.setFont(AppTheme.bodyFont());
        panel.add(confirmLabel);

        confirmPasswordField.setFont(AppTheme.bodyFont());
        panel.add(confirmPasswordField, "h 36!");

        errorLabel.setFont(AppTheme.captionFont());
        errorLabel.setForeground(AppTheme.DANGER);
        panel.add(errorLabel, "span 2, gaptop 5, gapbottom 10");

        JButton saveButton = new JButton("Reset Password");
        saveButton.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_BODY));
        saveButton.setBackground(AppTheme.PRIMARY);
        saveButton.setForeground(Color.WHITE);
        saveButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveButton.addActionListener(e -> onSave());

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(AppTheme.bodyFont());
        cancelButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new MigLayout("insets 0", "[grow,fill][grow,fill]"));
        buttonPanel.setOpaque(false);
        buttonPanel.add(saveButton, "h 40!");
        buttonPanel.add(cancelButton, "h 40!");

        panel.add(buttonPanel, "span 2, gaptop 10");
        return panel;
    }

    private void onSave() {
        char[] password = passwordField.getPassword();
        char[] confirm = confirmPasswordField.getPassword();

        if (password.length == 0) {
            errorLabel.setText("Password cannot be empty.");
            return;
        }

        if (!Arrays.equals(password, confirm)) {
            errorLabel.setText("Passwords do not match.");
            Arrays.fill(password, '\0');
            Arrays.fill(confirm, '\0');
            return;
        }

        try {
            context.userService().resetPassword(userToReset.id(), password, currentUser.id(), currentUser.username());
            saved = true;
            dispose();
        } catch (Exception ex) {
            errorLabel.setText(ex.getMessage());
        } finally {
            Arrays.fill(password, '\0');
            Arrays.fill(confirm, '\0');
        }
    }
}
