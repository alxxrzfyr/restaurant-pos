package com.restaurant.pos.ui.login;

import com.restaurant.pos.AppContext;
import com.restaurant.pos.model.User;
import com.restaurant.pos.ui.theme.AppTheme;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
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
        JPanel panel = new JPanel(new MigLayout("fillx, insets 20, wrap 2", "[right][grow,fill]"));
        panel.setBackground(AppTheme.BACKGROUND);

        JLabel titleLabel = new JLabel("Change Your Password");
        titleLabel.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_SECTION_HEADER));
        panel.add(titleLabel, "span 2, gapbottom 15");

        panel.add(new JLabel("New Password:"));
        panel.add(newPasswordField, "h 36!");

        panel.add(new JLabel("Confirm Password:"));
        panel.add(confirmPasswordField, "h 36!");

        errorLabel.setForeground(AppTheme.DANGER);
        panel.add(errorLabel, "span 2, gaptop 5, gapbottom 10");

        JButton saveBtn = new JButton("Change Password");
        saveBtn.setBackground(AppTheme.PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> onSave());

        JButton cancelBtn = new JButton("Cancel Login");
        cancelBtn.addActionListener(e -> dispose());

        panel.add(saveBtn, "span 2, split 2, growx");
        panel.add(cancelBtn, "growx");

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
            // clear the flag
            com.restaurant.pos.model.User updated = user.toBuilder().requiresPasswordChange(false).build();
            // wait, we can't update just the flag via userService easily without a new method.
            // Let's create `clearRequiresPasswordChange(long userId)` in UserService.
            context.userService().clearRequiresPasswordChange(user.id());
            
            changed = true;
            dispose();
        } finally {
            Arrays.fill(newPass, '\0');
            Arrays.fill(confirm, '\0');
        }
    }
}
