package com.restaurant.pos.ui.admin;

import com.restaurant.pos.AppContext;
import com.restaurant.pos.model.Category;
import com.restaurant.pos.model.MenuItem;
import com.restaurant.pos.model.Money;
import com.restaurant.pos.model.User;
import com.restaurant.pos.ui.theme.AppTheme;
import com.restaurant.pos.ui.theme.Icons;
import com.restaurant.pos.util.ImageStorage;
import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

final class MenuItemDialog extends JDialog {

    private final AppContext context;
    private final User currentUser;
    private final MenuItem itemToEdit;

    private final JTextField nameField = new JTextField(20);
    private final JComboBox<Category> categoryComboBox = new JComboBox<>();
    private final JTextField priceField = new JTextField(10);
    private final JTextField costField = new JTextField(10);
    private final JCheckBox availableCheckBox = new JCheckBox("Available for ordering", true);
    private final JLabel previewLabel = new JLabel();
    private final JLabel imagePathLabel = new JLabel("No image selected");
    private final JLabel errorLabel = new JLabel(" ");

    private String selectedImagePath = null;
    private boolean saved = false;

    MenuItemDialog(Frame owner, AppContext context, User currentUser, MenuItem itemToEdit) {
        super(owner, itemToEdit == null ? "Add Menu Item" : "Edit Menu Item", true);
        this.context = context;
        this.currentUser = currentUser;
        this.itemToEdit = itemToEdit;

        populateCategories();

        if (itemToEdit != null) {
            nameField.setText(itemToEdit.name());
            priceField.setText(itemToEdit.price().toPlainString());
            if (itemToEdit.cost() != null) {
                costField.setText(itemToEdit.cost().toPlainString());
            }
            availableCheckBox.setSelected(itemToEdit.available());
            selectCategoryById(itemToEdit.categoryId());
            if (itemToEdit.imagePath() != null) {
                setImagePath(itemToEdit.imagePath());
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

    private void populateCategories() {
        List<Category> categories = context.categoryService().findAllOrdered();
        categoryComboBox.removeAllItems();
        for (Category category : categories) {
            categoryComboBox.addItem(category);
        }
    }

    private void selectCategoryById(Long categoryId) {
        for (int i = 0; i < categoryComboBox.getItemCount(); i++) {
            Category category = categoryComboBox.getItemAt(i);
            if (category != null && category.id().equals(categoryId)) {
                categoryComboBox.setSelectedIndex(i);
                break;
            }
        }
    }

    private void setImagePath(String path) {
        this.selectedImagePath = path;
        if (path != null && !path.isBlank()) {
            File f = new File(path);
            if (f.exists()) {
                imagePathLabel.setText(f.getName());
                try {
                    BufferedImage img = ImageIO.read(f);
                    if (img != null) {
                        BufferedImage scaled = com.restaurant.pos.util.ImageStorage.scaleCenterCrop(img, 48, 48);
                        previewLabel.setIcon(new ImageIcon(scaled));
                        return;
                    }
                } catch (IOException ignored) {

                }
            }
        }
        imagePathLabel.setText("No image selected");
        previewLabel.setIcon(null);
    }

    private JPanel buildContent() {
        JPanel panel = new JPanel(new MigLayout("fillx, insets 20, wrap 2", "[right][grow,fill]"));
        panel.setBackground(AppTheme.BACKGROUND);

        JLabel titleLabel = new JLabel(itemToEdit == null ? "New Menu Item" : "Edit Menu Item");
        titleLabel.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_SECTION_HEADER));
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        panel.add(titleLabel, "span 2, gapbottom 15");

        JLabel nameLabel = new JLabel("Item Name:");
        nameLabel.setFont(AppTheme.bodyFont());
        panel.add(nameLabel);

        nameField.setFont(AppTheme.bodyFont());
        panel.add(nameField, "h 36!");

        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(AppTheme.bodyFont());
        panel.add(categoryLabel);

        categoryComboBox.setFont(AppTheme.bodyFont());
        panel.add(categoryComboBox, "h 36!");

        JLabel priceLabel = new JLabel("Price (\u20B1):");
        priceLabel.setFont(AppTheme.bodyFont());
        panel.add(priceLabel);

        priceField.setFont(AppTheme.bodyFont());
        panel.add(priceField, "h 36!");

        JLabel costLabel = new JLabel("Cost (\u20B1, optional):");
        costLabel.setFont(AppTheme.bodyFont());
        panel.add(costLabel);

        costField.setFont(AppTheme.bodyFont());
        panel.add(costField, "h 36!");

        JLabel imageLabel = new JLabel("Item Image:");
        imageLabel.setFont(AppTheme.bodyFont());
        panel.add(imageLabel);

        JPanel imagePickerBox = new JPanel(new MigLayout("insets 0", "[][][]"));
        imagePickerBox.setOpaque(false);

        previewLabel.setPreferredSize(new Dimension(48, 48));
        previewLabel.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER));
        imagePickerBox.add(previewLabel, "w 48!, h 48!");

        JButton browseBtn = new JButton("Upload Photo");
        browseBtn.setFont(AppTheme.bodyFont());
        browseBtn.setIcon(Icons.camera(AppTheme.TEXT_PRIMARY, 16));
        browseBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        browseBtn.addActionListener(e -> onChooseImage());
        imagePickerBox.add(browseBtn, "h 36!, gapleft 8");

        JButton clearImageBtn = new JButton("Remove");
        clearImageBtn.setFont(AppTheme.captionFont());
        clearImageBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearImageBtn.addActionListener(e -> setImagePath(null));
        imagePickerBox.add(clearImageBtn, "h 36!, gapleft 4");

        panel.add(imagePickerBox);

        availableCheckBox.setFont(AppTheme.bodyFont());
        availableCheckBox.setOpaque(false);
        panel.add(availableCheckBox, "span 2, gaptop 5");

        errorLabel.setFont(AppTheme.captionFont());
        errorLabel.setForeground(AppTheme.DANGER);
        panel.add(errorLabel, "span 2, gaptop 5, gapbottom 10");

        JButton saveButton = new JButton("Save Item");
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

    private void onChooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Upload Item Photo");
        chooser.setFileFilter(new FileNameExtensionFilter("Images (JPG, PNG, GIF, WEBP)", "jpg", "jpeg", "png", "gif", "webp"));
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File chosen = chooser.getSelectedFile();
            try {
                String savedPath = ImageStorage.saveItemImage(chosen);
                if (savedPath != null) {
                    setImagePath(savedPath);
                }
            } catch (IOException ex) {
                errorLabel.setText("Failed to save image: " + ex.getMessage());
            }
        }
    }

    private void onSave() {
        String name = nameField.getText().trim();
        Category selectedCategory = (Category) categoryComboBox.getSelectedItem();
        String priceStr = priceField.getText().trim();
        String costStr = costField.getText().trim();

        try {
            com.restaurant.pos.validation.Validator.requireValidName(name, "Item name");

            if (selectedCategory == null) {
                errorLabel.setText("Please select a category.");
                return;
            }

            com.restaurant.pos.validation.Validator.requireNonEmpty(priceStr, "Price");
            
            BigDecimal bdPrice = new BigDecimal(priceStr);
            com.restaurant.pos.validation.Validator.requireNonNegative(bdPrice, "Price");
            Money price = Money.of(bdPrice);

            Money cost = null;
            if (!costStr.isEmpty()) {
                BigDecimal bdCost = new BigDecimal(costStr);
                com.restaurant.pos.validation.Validator.requireNonNegative(bdCost, "Cost");
                cost = Money.of(bdCost);
            }

            boolean available = availableCheckBox.isSelected();
            if (itemToEdit == null) {
                MenuItem item = MenuItem.builder()
                        .name(name)
                        .categoryId(selectedCategory.id())
                        .categoryName(selectedCategory.name())
                        .price(price)
                        .cost(cost)
                        .available(available)
                        .imagePath(selectedImagePath)
                        .build();
                context.menuService().create(item, currentUser.id(), currentUser.username());
            } else {
                MenuItem updated = itemToEdit.toBuilder()
                        .name(name)
                        .categoryId(selectedCategory.id())
                        .categoryName(selectedCategory.name())
                        .price(price)
                        .cost(cost)
                        .available(available)
                        .imagePath(selectedImagePath)
                        .build();
                context.menuService().update(updated, currentUser.id(), currentUser.username());
            }
            saved = true;
            dispose();
        } catch (com.restaurant.pos.validation.ValidationException ex) {
            errorLabel.setText(ex.getMessage());
        } catch (NumberFormatException ex) {
            errorLabel.setText("Invalid number format (e.g. use 150.00).");
        } catch (Exception ex) {
            errorLabel.setText("Failed to save: " + ex.getMessage());
        }
    }
}
