package com.restaurant.pos.ui.admin;

import com.restaurant.pos.AppContext;
import com.restaurant.pos.model.Category;
import com.restaurant.pos.model.User;
import com.restaurant.pos.ui.theme.AppTheme;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Frame;

final class CategoryDialog extends JDialog {

    private final AppContext context;
    private final User currentUser;
    private final Category categoryToEdit;

    private final JTextField nameField = new JTextField(20);
    private final JSpinner displayOrderSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final JLabel errorLabel = new JLabel(" ");

    private boolean saved = false;

    CategoryDialog(Frame owner, AppContext context, User currentUser, Category categoryToEdit) {
        super(owner, categoryToEdit == null ? "Add Category" : "Edit Category", true);
        this.context = context;
        this.currentUser = currentUser;
        this.categoryToEdit = categoryToEdit;

        if (categoryToEdit != null) {
            nameField.setText(categoryToEdit.name());
            displayOrderSpinner.setValue(categoryToEdit.displayOrder());
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
        JPanel panel = new JPanel(new MigLayout("fillx, insets 20, wrap 2", "[right][grow,fill]"));
        panel.setBackground(AppTheme.BACKGROUND);

        JLabel titleLabel = new JLabel(categoryToEdit == null ? "New Category" : "Edit Category");
        titleLabel.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_SECTION_HEADER));
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        panel.add(titleLabel, "span 2, gapbottom 15");

        JLabel nameLabel = new JLabel("Category Name:");
        nameLabel.setFont(AppTheme.bodyFont());
        panel.add(nameLabel);

        nameField.setFont(AppTheme.bodyFont());
        panel.add(nameField, "h 36!");

        JLabel orderLabel = new JLabel("Display Order:");
        orderLabel.setFont(AppTheme.bodyFont());
        panel.add(orderLabel);

        displayOrderSpinner.setFont(AppTheme.bodyFont());
        panel.add(displayOrderSpinner, "h 36!");

        errorLabel.setFont(AppTheme.captionFont());
        errorLabel.setForeground(AppTheme.DANGER);
        panel.add(errorLabel, "span 2, gaptop 5, gapbottom 10");

        JButton saveButton = new JButton("Save Category");
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
        String name = nameField.getText().trim();
        int displayOrder = (Integer) displayOrderSpinner.getValue();

        try {
            com.restaurant.pos.validation.Validator.requireValidName(name, "Category name");
            if (categoryToEdit == null) {
                context.categoryService().create(name, displayOrder, currentUser.id(), currentUser.username());
            } else {
                Category updated = categoryToEdit.toBuilder()
                        .name(name)
                        .displayOrder(displayOrder)
                        .build();
                context.categoryService().update(updated, currentUser.id(), currentUser.username());
            }
            saved = true;
            dispose();
        } catch (com.restaurant.pos.validation.ValidationException ex) {
            errorLabel.setText(ex.getMessage());
        } catch (Exception ex) {
            errorLabel.setText("Failed to save: " + ex.getMessage());
        }
    }
}
