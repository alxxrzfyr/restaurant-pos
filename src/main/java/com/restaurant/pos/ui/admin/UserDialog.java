package com.restaurant.pos.ui.admin;

import com.restaurant.pos.AppContext;
import com.restaurant.pos.exception.AuthenticationException;
import com.restaurant.pos.exception.DuplicateUsernameException;
import com.restaurant.pos.model.Role;
import com.restaurant.pos.model.User;
import com.restaurant.pos.ui.theme.AppTheme;
import com.restaurant.pos.ui.theme.Icons;
import com.restaurant.pos.util.ImageStorage;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

final class UserDialog extends JDialog {

    private final AppContext context;
    private final User currentUser;
    private final User targetUser;

    private final JTextField usernameField = new JTextField(20);
    private final JTextField displayNameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JPasswordField confirmPasswordField = new JPasswordField(20);
    private final JComboBox<Role> roleComboBox = new JComboBox<>(Role.values());
    private final JPasswordField adminPasswordField = new JPasswordField(20);

    private final JLabel photoPathLabel = new JLabel("No photo selected");
    private final JButton removePhotoBtn = new JButton("Remove");
    private final JLabel errorLabel = new JLabel(" ");

    private String selectedPhotoPath = null;
    private boolean saved = false;

    UserDialog(Frame owner, AppContext context, User currentUser) {
        this(owner, context, currentUser, null);
    }

    UserDialog(Frame owner, AppContext context, User currentUser, User targetUser) {
        super(owner, targetUser == null ? "Add User Account" : "Edit User: " + targetUser.username(), true);
        this.context = context;
        this.currentUser = currentUser;
        this.targetUser = targetUser;

        if (targetUser != null) {
            this.selectedPhotoPath = targetUser.photoPath();
            this.usernameField.setText(targetUser.username());
            this.usernameField.setEnabled(false);
            this.displayNameField.setText(targetUser.displayName());
            this.roleComboBox.setSelectedItem(targetUser.role());
            if (targetUser.photoPath() != null && !targetUser.photoPath().isBlank()) {
                this.photoPathLabel.setText(new File(targetUser.photoPath()).getName());
            }
        }

        setContentPane(buildContent());
        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    boolean isSaved() {
        return saved;
    }

    private JPanel buildContent() {
        JPanel panel = new JPanel(new MigLayout("fillx, insets 24, wrap 2", "[right, 140!][grow,fill]"));
        panel.setBackground(AppTheme.CARD);

        JLabel titleLabel = new JLabel(targetUser == null ? "New User Account" : "Edit User Account");
        titleLabel.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_SECTION_HEADER));
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        panel.add(titleLabel, "span 2, gapbottom 12");

        panel.add(createLabel("Username:"));
        usernameField.setFont(AppTheme.bodyFont());
        panel.add(usernameField, "h 36!");

        panel.add(createLabel("Display Name:"));
        displayNameField.setFont(AppTheme.bodyFont());
        panel.add(displayNameField, "h 36!");

        panel.add(createLabel(targetUser == null ? "Password:" : "New Password:"));
        passwordField.setFont(AppTheme.bodyFont());
        panel.add(passwordField, "h 36!");

        panel.add(createLabel(targetUser == null ? "Confirm Password:" : "Confirm New Password:"));
        confirmPasswordField.setFont(AppTheme.bodyFont());
        panel.add(confirmPasswordField, "h 36!");

        if (targetUser != null) {
            JLabel pwdNote = new JLabel("Leave password fields blank to keep current password.");
            pwdNote.setFont(AppTheme.captionFont());
            pwdNote.setForeground(AppTheme.TEXT_MUTED);
            panel.add(pwdNote, "span 2, gapleft 140, gapbottom 4");
        }

        panel.add(createLabel("Role:"));
        roleComboBox.setFont(AppTheme.bodyFont());
        panel.add(roleComboBox, "h 36!");

        panel.add(createLabel("Profile Photo:"));
        JPanel photoBox = new JPanel(new MigLayout("insets 0", "[][][]"));
        photoBox.setOpaque(false);

        JButton uploadBtn = new JButton("Choose Photo");
        uploadBtn.setFont(AppTheme.bodyFont());
        uploadBtn.setIcon(Icons.camera(AppTheme.TEXT_PRIMARY, 14));
        uploadBtn.setIconTextGap(6);
        uploadBtn.setBackground(AppTheme.CARD);
        uploadBtn.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER));
        uploadBtn.setFocusPainted(false);
        uploadBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        uploadBtn.addActionListener(e -> onChoosePhoto());
        photoBox.add(uploadBtn, "h 36!");

        removePhotoBtn.setFont(AppTheme.bodyFont());
        removePhotoBtn.setBackground(AppTheme.DANGER_BG);
        removePhotoBtn.setForeground(AppTheme.DANGER);
        removePhotoBtn.setBorder(BorderFactory.createLineBorder(AppTheme.DANGER_BORDER));
        removePhotoBtn.setFocusPainted(false);
        removePhotoBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        removePhotoBtn.setVisible(selectedPhotoPath != null);
        removePhotoBtn.addActionListener(e -> {
            selectedPhotoPath = null;
            photoPathLabel.setText("No photo selected");
            removePhotoBtn.setVisible(false);
        });
        photoBox.add(removePhotoBtn, "h 36!, gapleft 6");

        photoPathLabel.setFont(AppTheme.captionFont());
        photoPathLabel.setForeground(AppTheme.TEXT_SECONDARY);
        photoBox.add(photoPathLabel, "gapleft 10");
        panel.add(photoBox);

        JLabel adminPassLabel = createLabel("Admin Password:");
        adminPassLabel.setForeground(AppTheme.ACCENT);
        panel.add(adminPassLabel, "gaptop 8");

        adminPasswordField.setFont(AppTheme.bodyFont());
        panel.add(adminPasswordField, "h 36!, gaptop 8");

        JLabel adminNote = new JLabel("Enter your admin password to authorize changes.");
        adminNote.setFont(AppTheme.captionFont());
        adminNote.setForeground(AppTheme.TEXT_MUTED);
        panel.add(adminNote, "span 2, gapleft 140, gapbottom 4");

        errorLabel.setFont(AppTheme.captionFont());
        errorLabel.setForeground(AppTheme.DANGER);
        panel.add(errorLabel, "span 2, gaptop 4, gapbottom 10");

        JButton saveButton = new JButton(targetUser == null ? "Create User" : "Save Changes");
        saveButton.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_BODY));
        saveButton.setIcon(Icons.check(Color.WHITE, 16));
        saveButton.setIconTextGap(6);
        saveButton.setBackground(AppTheme.PRIMARY);
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveButton.addActionListener(e -> onSave());

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(AppTheme.bodyFont());
        cancelButton.setBackground(AppTheme.CARD);
        cancelButton.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER));
        cancelButton.setFocusPainted(false);
        cancelButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new MigLayout("insets 0", "[grow,fill]10[grow,fill]"));
        buttonPanel.setOpaque(false);
        buttonPanel.add(cancelButton, "h 40!");
        buttonPanel.add(saveButton, "h 40!");

        panel.add(buttonPanel, "span 2, gaptop 10");

        adminPasswordField.addActionListener(e -> onSave());

        return panel;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(AppTheme.bodyFont());
        label.setForeground(AppTheme.TEXT_SECONDARY);
        return label;
    }

    private void onChoosePhoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Profile Photo");
        chooser.setFileFilter(new FileNameExtensionFilter("Images (JPG, PNG, GIF, WEBP)", "jpg", "jpeg", "png", "gif", "webp"));
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File chosen = chooser.getSelectedFile();
            try {
                String savedPath = ImageStorage.saveProfilePhoto(chosen);
                if (savedPath != null) {
                    selectedPhotoPath = savedPath;
                    photoPathLabel.setText(chosen.getName());
                    removePhotoBtn.setVisible(true);
                }
            } catch (IOException ex) {
                errorLabel.setText("Failed to save photo: " + ex.getMessage());
            }
        }
    }

    private void onSave() {
        String username = usernameField.getText().trim();
        String displayName = displayNameField.getText().trim();
        try {
            com.restaurant.pos.validation.Validator.requireValidName(username, "Username");
            com.restaurant.pos.validation.Validator.requireValidName(displayName, "Display name");
        } catch (com.restaurant.pos.validation.ValidationException ex) {
            errorLabel.setText(ex.getMessage());
            return;
        }

        char[] password = passwordField.getPassword();
        char[] confirm = confirmPasswordField.getPassword();

        if (targetUser == null && password.length == 0) {
            errorLabel.setText("Password cannot be empty.");
            return;
        }

        if (password.length > 0 && !Arrays.equals(password, confirm)) {
            errorLabel.setText("Passwords do not match.");
            Arrays.fill(password, '\0');
            Arrays.fill(confirm, '\0');
            return;
        }

        char[] adminPassword = adminPasswordField.getPassword();
        if (adminPassword.length == 0) {
            errorLabel.setText("Admin password is required to save changes.");
            return;
        }

        try {
            context.authService().login(currentUser.username(), adminPassword);
        } catch (AuthenticationException ex) {
            errorLabel.setText("Incorrect Admin password. Changes denied.");
            Arrays.fill(adminPassword, '\0');
            return;
        } catch (Exception ex) {
            errorLabel.setText("Authentication failed: " + ex.getMessage());
            Arrays.fill(adminPassword, '\0');
            return;
        } finally {
            Arrays.fill(adminPassword, '\0');
        }

        Role role = (Role) roleComboBox.getSelectedItem();

        try {
            if (targetUser == null) {
                context.userService().create(username, displayName, password, role, selectedPhotoPath, currentUser.id(), currentUser.username());
            } else {
                context.userService().updateUser(targetUser.id(), displayName, selectedPhotoPath, currentUser.id(), currentUser.username());
                if (role != targetUser.role()) {
                    context.userService().changeRole(targetUser.id(), role, currentUser.id(), currentUser.username());
                }
                if (password.length > 0) {
                    context.userService().resetPassword(targetUser.id(), password, currentUser.id(), currentUser.username());
                }
            }
            saved = true;
            dispose();
        } catch (DuplicateUsernameException ex) {
            errorLabel.setText("Username '" + username + "' is already taken.");
        } catch (Exception ex) {
            errorLabel.setText(ex.getMessage());
        } finally {
            Arrays.fill(password, '\0');
            Arrays.fill(confirm, '\0');
        }
    }
}
