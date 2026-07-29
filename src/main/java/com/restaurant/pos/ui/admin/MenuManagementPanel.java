package com.restaurant.pos.ui.admin;

import com.restaurant.pos.AppContext;
import com.restaurant.pos.exception.CategoryInUseException;
import com.restaurant.pos.model.Category;
import com.restaurant.pos.model.MenuItem;
import com.restaurant.pos.model.User;
import com.restaurant.pos.ui.format.MoneyFormatter;
import com.restaurant.pos.ui.theme.AppTheme;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Frame;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class MenuManagementPanel extends JPanel {

    private static final int ROW_HEIGHT = 34;

    private final AppContext context;
    private final User currentUser;

    private final CategoryTableModel categoryTableModel = new CategoryTableModel();
    private final MenuItemTableModel menuItemTableModel = new MenuItemTableModel();

    private final JTable categoryTable = new JTable(categoryTableModel);
    private final JTable menuItemTable = new JTable(menuItemTableModel);

    private List<MenuItem> allMenuItems = new ArrayList<>();

    public MenuManagementPanel(AppContext context, User currentUser) {
        super(new BorderLayout(0, 12));
        this.context = context;
        this.currentUser = currentUser;

        setBackground(AppTheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(buildHeaderBar(), BorderLayout.NORTH);
        add(buildTablesPane(), BorderLayout.CENTER);

        loadData();
    }

    private JPanel buildHeaderBar() {
        JPanel header = new JPanel(new MigLayout("insets 0, fillx", "[grow][]"));
        header.setOpaque(false);

        JLabel title = new JLabel("Menu & Category Management");
        title.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_PAGE_TITLE));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        header.add(title);

        JPanel toolbar = new JPanel(new MigLayout("insets 0", "[][][][][][]"));
        toolbar.setOpaque(false);

        JButton addCatBtn = createButton("Add Category", false);
        addCatBtn.addActionListener(e -> onAddCategory());

        JButton editCatBtn = createButton("Edit Category", false);
        editCatBtn.addActionListener(e -> onEditCategory());

        JButton delCatBtn = createButton("Delete Category", true);
        delCatBtn.addActionListener(e -> onDeleteCategory());

        JButton addItemBtn = createButton("Add Item", false);
        addItemBtn.addActionListener(e -> onAddItem());

        JButton editItemBtn = createButton("Edit Item", false);
        editItemBtn.addActionListener(e -> onEditItem());

        JButton toggle86Btn = createButton("Toggle 86 (Availability)", false);
        toggle86Btn.addActionListener(e -> onToggleAvailability());

        toolbar.add(addCatBtn, "h 38!");
        toolbar.add(editCatBtn, "h 38!");
        toolbar.add(delCatBtn, "h 38!");
        toolbar.add(addItemBtn, "h 38!, gapleft 15");
        toolbar.add(editItemBtn, "h 38!");
        toolbar.add(toggle86Btn, "h 38!");

        header.add(toolbar);
        return header;
    }

    private JButton createButton(String text, boolean danger) {
        JButton button = new JButton(text);
        button.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_BODY));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (danger) {
            button.setBackground(AppTheme.DANGER);
            button.setForeground(Color.WHITE);
        }
        return button;
    }

    private JSplitPane buildTablesPane() {
        categoryTable.setRowHeight(ROW_HEIGHT);
        categoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryTable.getTableHeader().setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_TABLE_HEADER));
        categoryTable.setFont(AppTheme.bodyFont());
        com.restaurant.pos.ui.components.StripedTableCellRenderer.apply(categoryTable);
        categoryTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                filterItemsBySelectedCategory();
            }
        });

        menuItemTable.setRowHeight(ROW_HEIGHT);
        menuItemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        menuItemTable.getTableHeader().setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_TABLE_HEADER));
        menuItemTable.setFont(AppTheme.bodyFont());
        com.restaurant.pos.ui.components.StripedTableCellRenderer.apply(menuItemTable);

        JPanel catPanel = new JPanel(new BorderLayout(0, 8));
        catPanel.setOpaque(false);
        JLabel catTitle = new JLabel("Categories");
        catTitle.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_SECTION_HEADER));
        catPanel.add(catTitle, BorderLayout.NORTH);
        catPanel.add(new JScrollPane(categoryTable), BorderLayout.CENTER);

        JPanel itemPanel = new JPanel(new BorderLayout(0, 8));
        itemPanel.setOpaque(false);
        JLabel itemTitle = new JLabel("Menu Items");
        itemTitle.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_SECTION_HEADER));
        itemPanel.add(itemTitle, BorderLayout.NORTH);
        itemPanel.add(new JScrollPane(menuItemTable), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, catPanel, itemPanel);
        splitPane.setDividerLocation(340);
        splitPane.setOpaque(false);
        return splitPane;
    }

    private void loadData() {
        List<Category> categories = context.categoryService().findAllOrdered();
        categoryTableModel.setCategories(categories);

        allMenuItems = context.menuService().findAll();
        filterItemsBySelectedCategory();
    }

    private void filterItemsBySelectedCategory() {
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < categoryTableModel.getRowCount()) {
            Category cat = categoryTableModel.getCategoryAt(selectedRow);
            List<MenuItem> filtered = allMenuItems.stream()
                    .filter(item -> Objects.equals(item.categoryId(), cat.id()))
                    .toList();
            menuItemTableModel.setItems(filtered);
        } else {
            menuItemTableModel.setItems(allMenuItems);
        }
    }

    private Frame getParentFrame() {
        return (Frame) SwingUtilities.getWindowAncestor(this);
    }

    private void onAddCategory() {
        CategoryDialog dialog = new CategoryDialog(getParentFrame(), context, currentUser, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadData();
        }
    }

    private void onEditCategory() {
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a category to edit.", "Select Category", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Category category = categoryTableModel.getCategoryAt(selectedRow);
        CategoryDialog dialog = new CategoryDialog(getParentFrame(), context, currentUser, category);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadData();
        }
    }

    private void onDeleteCategory() {
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a category to delete.", "Select Category", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Category category = categoryTableModel.getCategoryAt(selectedRow);
        int choice = JOptionPane.showConfirmDialog(this,
                "Delete category '" + category.name() + "'?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                context.categoryService().delete(category.id(), currentUser.id(), currentUser.username());
                loadData();
            } catch (CategoryInUseException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Delete Category", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting category: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onAddItem() {
        if (context.categoryService().findAllOrdered().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please create at least one category before adding items.", "No Categories", JOptionPane.WARNING_MESSAGE);
            return;
        }
        MenuItemDialog dialog = new MenuItemDialog(getParentFrame(), context, currentUser, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadData();
        }
    }

    private void onEditItem() {
        int selectedRow = menuItemTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a menu item to edit.", "Select Item", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        MenuItem item = menuItemTableModel.getItemAt(selectedRow);
        MenuItemDialog dialog = new MenuItemDialog(getParentFrame(), context, currentUser, item);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadData();
        }
    }

    private void onToggleAvailability() {
        int selectedRow = menuItemTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a menu item to toggle availability.", "Select Item", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        MenuItem item = menuItemTableModel.getItemAt(selectedRow);
        boolean newAvailability = !item.available();
        try {
            context.menuService().setAvailability(item.id(), newAvailability, currentUser.id(), currentUser.username());
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating availability: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static final class CategoryTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {"ID", "Name", "Order"};
        private List<Category> categories = new ArrayList<>();

        void setCategories(List<Category> categories) {
            this.categories = new ArrayList<>(categories);
            fireTableDataChanged();
        }

        Category getCategoryAt(int row) {
            return categories.get(row);
        }

        @Override
        public int getRowCount() {
            return categories.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Category cat = categories.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> cat.id();
                case 1 -> cat.name();
                case 2 -> cat.displayOrder();
                default -> "";
            };
        }
    }

    private static final class MenuItemTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {"ID", "Name", "Category", "Price", "Cost", "Status"};
        private List<MenuItem> items = new ArrayList<>();

        void setItems(List<MenuItem> items) {
            this.items = new ArrayList<>(items);
            fireTableDataChanged();
        }

        MenuItem getItemAt(int row) {
            return items.get(row);
        }

        @Override
        public int getRowCount() {
            return items.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            MenuItem item = items.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> item.id();
                case 1 -> item.name();
                case 2 -> item.categoryName() != null ? item.categoryName() : item.categoryId();
                case 3 -> MoneyFormatter.format(item.price());
                case 4 -> item.cost() != null ? MoneyFormatter.format(item.cost()) : "-";
                case 5 -> item.available() ? "Available" : "86'd (Unavailable)";
                default -> "";
            };
        }
    }
}
