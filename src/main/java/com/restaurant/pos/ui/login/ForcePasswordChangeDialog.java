package com.restaurant.pos.ui.login;

import com.restaurant.pos.AppContext;
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

public class ForcePasswordChangeDialog extends JDialog {

    private final AppContext context;
    private final User user;
    private final JPasswordField newPasswordField = new JPasswordField(20);
    private final JPasswordField confirmPasswordField = new JPasswordField(20);
    private final JLabel errorLabel = new JLabel(" ");
    private boolean changed = false;

    @SuppressWarnings("this-escape")
    public ForcePasswordChangeDialog(Frame owner, AppContext context, User user) {
        super(owner, "Password Change Required", true);
        this.context = context;
        this.user = user;

        setContentPane(buildContent());
        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    public boolean isChanged() {
        return changed;
    }

    private JPanel buildContent() {
        JPanel panel = new JPanel(new MigLayout("fillx, insets 24, wrap 2", "[right, 140!][grow,fill]"));
        panel.setBackground(AppTheme.CARD);

        JLabel titleLabel = new JLabel("Change Your Password");
        titleLabel.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_SECTION_HEADER));
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        panel.add(titleLabel, "span 2, gapbottom 12");

        JLabel newPassLabel = new JLabel("New Password:");
        newPassLabel.setFont(AppTheme.bodyFont());
        newPassLabel.setForeground(AppTheme.TEXT_SECONDARY);
        panel.add(newPassLabel);

        newPasswordField.setFont(AppTheme.bodyFont());
        panel.add(newPasswordField, "h 36!");

        JLabel confirmLabel = new JLabel("Confirm Password:");
        confirmLabel.setFont(AppTheme.bodyFont());
        confirmLabel.setForeground(AppTheme.TEXT_SECONDARY);
        panel.add(confirmLabel);

        confirmPasswordField.setFont(AppTheme.bodyFont());
        panel.add(confirmPasswordField, "h 36!");

        errorLabel.setFont(AppTheme.captionFont());
        errorLabel.setForeground(AppTheme.DANGER);
        panel.add(errorLabel, "span 2, gaptop 4, gapbottom 10");

        JButton saveBtn = new JButton("Update Password");
        saveBtn.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_BODY));
        saveBtn.setIcon(Icons.check(Color.WHITE, 16));
        saveBtn.setIconTextGap(6);
        saveBtn.setBackground(AppTheme.PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveBtn.addActionListener(e -> onSave());

        JButton cancelBtn = new JButton("Cancel Login");
        cancelBtn.setFont(AppTheme.bodyFont());
        cancelBtn.setBackground(AppTheme.CARD);
        cancelBtn.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new MigLayout("insets 0", "[grow,fill]10[grow,fill]"));
        buttonPanel.setOpaque(false);
        buttonPanel.add(cancelBtn, "h 40!");
        buttonPanel.add(saveBtn, "h 40!");

        panel.add(buttonPanel, "span 2, gaptop 10");

        return panel;
    }

    private void onSave() {
        char[] newPass = newPasswordField.getPassword();
        char[] confirm = confirmPasswordField.getPassword();

        try {
            if (newPass.length < 6) {
                errorLabel.setText("Password must be at least 6 characters.");
                return;
            }
            if (!Arrays.equals(newPass, confirm)) {
                errorLabel.setText("Passwords do not match.");
                return;
            }
            context.userService().resetPassword(user.id(), newPass, user.id(), user.username());
            context.userService().clearRequiresPasswordChange(user.id());

            changed = true;
            dispose();
        } finally {
            Arrays.fill(newPass, '\0');
            Arrays.fill(confirm, '\0');
        }
    }
}
