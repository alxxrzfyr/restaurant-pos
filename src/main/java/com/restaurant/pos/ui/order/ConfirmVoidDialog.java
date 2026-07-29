package com.restaurant.pos.ui.order;

import com.restaurant.pos.AppContext;
import com.restaurant.pos.exception.AuthenticationException;
import com.restaurant.pos.model.User;
import com.restaurant.pos.ui.theme.AppTheme;
import com.restaurant.pos.ui.theme.Icons;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Frame;
import java.util.Arrays;

public final class ConfirmVoidDialog extends JDialog {

    private final AppContext context;
    private final User currentUser;
    private final String orderNumber;

    private final JTextField reasonField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JLabel errorLabel = new JLabel(" ");

    private String confirmedReason = null;

    public ConfirmVoidDialog(Frame owner, AppContext context, User currentUser, String orderNumber) {
        super(owner, "Confirm Void - Order #" + orderNumber, true);
        this.context = context;
        this.currentUser = currentUser;
        this.orderNumber = orderNumber;

        setContentPane(buildContent());
        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    public String getConfirmedReason() {
        return confirmedReason;
    }

    private JPanel buildContent() {
        JPanel panel = new JPanel(new MigLayout("fillx, insets 24, wrap 1", "[340!]"));
        panel.setBackground(AppTheme.CARD);

        JLabel title = new JLabel("Void Order #" + orderNumber);
        title.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_SECTION_HEADER));
        title.setForeground(AppTheme.DANGER);
        panel.add(title, "gapbottom 8");

        JLabel info = new JLabel("Re-enter your password to authorize this void action:");
        info.setFont(AppTheme.bodyFont());
        panel.add(info, "gapbottom 12");

        JLabel reasonLabel = new JLabel("Reason for Void:");
        reasonLabel.setFont(AppTheme.bodyFont());
        panel.add(reasonLabel, "gapbottom 4");

        reasonField.setFont(AppTheme.bodyFont());
        panel.add(reasonField, "growx, h 36!, gapbottom 12");

        JLabel passLabel = new JLabel("Admin Password:");
        passLabel.setFont(AppTheme.bodyFont());
        panel.add(passLabel, "gapbottom 4");

        passwordField.setFont(AppTheme.bodyFont());
        panel.add(passwordField, "growx, h 36!, gapbottom 6");

        errorLabel.setFont(AppTheme.captionFont());
        errorLabel.setForeground(AppTheme.DANGER);
        panel.add(errorLabel, "gapbottom 16");

        JPanel buttonPanel = new JPanel(new MigLayout("insets 0", "[grow]12[grow]"));
        buttonPanel.setOpaque(false);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(AppTheme.bodyFont());
        cancelBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dispose());

        JButton confirmBtn = new JButton("Void Order");
        confirmBtn.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_BODY));
        confirmBtn.setIcon(Icons.xCircle(Color.WHITE, 16));
        confirmBtn.setIconTextGap(8);
        confirmBtn.setBackground(AppTheme.DANGER);
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        confirmBtn.addActionListener(e -> onConfirmVoid());

        buttonPanel.add(cancelBtn, "growx, h 40!");
        buttonPanel.add(confirmBtn, "growx, h 40!");

        panel.add(buttonPanel, "growx");

        passwordField.addActionListener(e -> onConfirmVoid());

        return panel;
    }

    private void onConfirmVoid() {
        String reason = reasonField.getText().trim();
        if (reason.isEmpty()) {
            errorLabel.setText("Please enter a reason for voiding.");
            return;
        }

        char[] password = passwordField.getPassword();
        if (password.length == 0) {
            errorLabel.setText("Password cannot be empty.");
            return;
        }

        try {
            context.authService().login(currentUser.username(), password);
            confirmedReason = reason;
            dispose();
        } catch (AuthenticationException ex) {
            errorLabel.setText("Incorrect password. Void denied.");
        } catch (Exception ex) {
            errorLabel.setText(ex.getMessage());
        } finally {
            Arrays.fill(password, '\0');
        }
    }
}
