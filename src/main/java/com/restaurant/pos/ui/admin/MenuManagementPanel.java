package com.restaurant.pos.ui.admin;

import com.restaurant.pos.AppContext;
import com.restaurant.pos.model.Category;
import com.restaurant.pos.model.MenuItem;
import com.restaurant.pos.model.User;
import com.restaurant.pos.ui.format.MoneyFormatter;
import com.restaurant.pos.ui.theme.AppTheme;
import com.restaurant.pos.ui.theme.Icons;
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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class MenuManagementPanel extends JPanel {

    private static final int ROW_HEIGHT = 38;

    private final AppContext context;
    private final User currentUser;

    private final CategoryTableModel categoryTableModel = new CategoryTableModel();
    private final MenuItemTableModel menuItemTableModel = new MenuItemTableModel();

    private final JTable categoryTable = new JTable(categoryTableModel);
    private final JTable menuItemTable = new JTable(menuItemTableModel);

    private final JLabel catCountLabel = new JLabel();
    private final JLabel itemCountLabel = new JLabel();

    private List<MenuItem> allMenuItems = new ArrayList<>();

    public MenuManagementPanel(AppContext context, User currentUser) {
        super(new BorderLayout(0, 16));
        this.context = context;
        this.currentUser = currentUser;

        setBackground(AppTheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(buildHeaderBar(), BorderLayout.NORTH);
        add(buildTablesPane(), BorderLayout.CENTER);

        loadData();
    }

    private JPanel buildHeaderBar() {
        JPanel header = new JPanel(new MigLayout("insets 0, fillx", "[grow][]"));
        header.setOpaque(false);

        JPanel titleBox = new JPanel(new MigLayout("insets 0, wrap 1, gapy 2"));
        titleBox.setOpaque(false);

        JLabel title = new JLabel("Menu & Category Management");
        title.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_PAGE_TITLE));
        title.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Organize categories, food items, pricing, costs, and availability status");
        subtitle.setFont(AppTheme.captionFont());
        subtitle.setForeground(AppTheme.TEXT_SECONDARY);

        titleBox.add(title);
        titleBox.add(subtitle);
        header.add(titleBox, "growx");

        return header;
    }

    private JButton createButton(String text, javax.swing.Icon icon, boolean danger, boolean primary) {
        JButton button = new JButton(text);
        if (icon != null) {
            button.setIcon(icon);
            button.setIconTextGap(8);
        }
        button.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_BODY));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);

        if (primary) {
            button.setBackground(AppTheme.PRIMARY);
            button.setForeground(Color.WHITE);
            button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        } else if (danger) {
            button.setBackground(AppTheme.DANGER_BG);
            button.setForeground(AppTheme.DANGER);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppTheme.DANGER_BORDER, 1),
                    BorderFactory.createEmptyBorder(7, 14, 7, 14)));
        } else {
            button.setBackground(AppTheme.CARD);
            button.setForeground(AppTheme.TEXT_PRIMARY);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                    BorderFactory.createEmptyBorder(7, 14, 7, 14)));
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

        categoryTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        categoryTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        categoryTable.getColumnModel().getColumn(2).setPreferredWidth(60);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < 3; i++) {
            categoryTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        menuItemTable.setRowHeight(ROW_HEIGHT);
        menuItemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        menuItemTable.getTableHeader().setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_TABLE_HEADER));
        menuItemTable.setFont(AppTheme.bodyFont());
        com.restaurant.pos.ui.components.StripedTableCellRenderer.apply(menuItemTable);

        menuItemTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        menuItemTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        menuItemTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        menuItemTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        menuItemTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        menuItemTable.getColumnModel().getColumn(5).setPreferredWidth(120);

        for (int i = 0; i < 5; i++) {
            menuItemTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        menuItemTable.getColumnModel().getColumn(5).setCellRenderer(new RoundedPillAvailabilityRenderer());

        JPanel catPanel = new JPanel(new BorderLayout(0, 12));
        catPanel.setBackground(AppTheme.CARD);
        catPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        catPanel.setMinimumSize(new Dimension(340, 200));

        JPanel catHeader = new JPanel(new MigLayout("insets 0, fillx, wrap 1", "[grow, fill]", "[]8[]"));
        catHeader.setOpaque(false);

        JPanel catTitleRow = new JPanel(new MigLayout("insets 0, fillx", "[grow][]"));
        catTitleRow.setOpaque(false);
        JLabel catTitle = new JLabel("Categories");
        catTitle.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_SECTION_HEADER));
        catTitle.setForeground(AppTheme.TEXT_PRIMARY);
        catCountLabel.setFont(AppTheme.captionFont());
        catCountLabel.setForeground(AppTheme.TEXT_MUTED);
        catTitleRow.add(catTitle, "growx");
        catTitleRow.add(catCountLabel);
        catHeader.add(catTitleRow, "growx");

        JPanel catActions = new JPanel(new MigLayout("insets 0, fillx", "[grow]6[grow]6[grow]"));
        catActions.setOpaque(false);
        JButton addCatBtn = createButton("Add", Icons.plus(Color.WHITE, 14), false, true);
        addCatBtn.addActionListener(e -> onAddCategory());
        JButton editCatBtn = createButton("Edit", Icons.edit(AppTheme.TEXT_PRIMARY, 14), false, false);
        editCatBtn.addActionListener(e -> onEditCategory());
        JButton delCatBtn = createButton("Delete", Icons.trash(AppTheme.DANGER, 14), true, false);
        delCatBtn.addActionListener(e -> onDeleteCategory());

        catActions.add(addCatBtn, "growx, h 34!");
        catActions.add(editCatBtn, "growx, h 34!");
        catActions.add(delCatBtn, "growx, h 34!");
        catHeader.add(catActions, "growx");

        catPanel.add(catHeader, BorderLayout.NORTH);
        catPanel.add(new JScrollPane(categoryTable), BorderLayout.CENTER);

        JPanel itemPanel = new JPanel(new BorderLayout(0, 12));
        itemPanel.setBackground(AppTheme.CARD);
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));

        JPanel itemHeader = new JPanel(new BorderLayout(8, 0));
        itemHeader.setOpaque(false);

        JPanel itemTitleBox = new JPanel(new MigLayout("insets 0, wrap 1, gapy 2"));
        itemTitleBox.setOpaque(false);
        JLabel itemTitle = new JLabel("Menu Items");
        itemTitle.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_SECTION_HEADER));
        itemTitle.setForeground(AppTheme.TEXT_PRIMARY);
        itemCountLabel.setFont(AppTheme.captionFont());
        itemCountLabel.setForeground(AppTheme.TEXT_MUTED);
        itemTitleBox.add(itemTitle);
        itemTitleBox.add(itemCountLabel);
        itemHeader.add(itemTitleBox, BorderLayout.WEST);

        JPanel itemActions = new JPanel(new MigLayout("insets 0", "[]8[]8[]"));
        itemActions.setOpaque(false);
        JButton addItemBtn = createButton("Add Item", Icons.plus(Color.WHITE, 14), false, true);
        addItemBtn.addActionListener(e -> onAddItem());
        JButton editItemBtn = createButton("Edit Item", Icons.edit(AppTheme.TEXT_PRIMARY, 14), false, false);
        editItemBtn.addActionListener(e -> onEditItem());
        JButton toggle86Btn = createButton("Toggle 86'd", Icons.toggleOn(AppTheme.TEXT_PRIMARY, 14), false, false);
        toggle86Btn.addActionListener(e -> onToggleAvailability());

        itemActions.add(addItemBtn, "h 36!");
        itemActions.add(editItemBtn, "h 36!");
        itemActions.add(toggle86Btn, "h 36!");
        itemHeader.add(itemActions, BorderLayout.EAST);

        itemPanel.add(itemHeader, BorderLayout.NORTH);
        itemPanel.add(new JScrollPane(menuItemTable), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, catPanel, itemPanel);
        splitPane.setDividerLocation(360);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        return splitPane;
    }

    private void loadData() {
        List<Category> categories = context.categoryService().findAllOrdered();
        categoryTableModel.setCategories(categories);
        catCountLabel.setText(categories.size() + " configured");

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
            itemCountLabel.setText("Showing " + filtered.size() + " items in " + cat.name());
        } else {
            menuItemTableModel.setItems(allMenuItems);
            itemCountLabel.setText("Showing all " + allMenuItems.size() + " items");
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
        Category cat = categoryTableModel.getCategoryAt(selectedRow);
        CategoryDialog dialog = new CategoryDialog(getParentFrame(), context, currentUser, cat);
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
        Category cat = categoryTableModel.getCategoryAt(selectedRow);

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete category '" + cat.name() + "'?\n" +
                "Any menu items in this category may become unassigned.",
                "Confirm Category Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                context.categoryService().delete(cat.id(), currentUser.id(), currentUser.username());
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting category: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onAddItem() {
        MenuItemDialog dialog = new MenuItemDialog(getParentFrame(), context, currentUser, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadData();
        }
    }

    private void onEditItem() {
        int selectedRow = menuItemTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item to edit.", "Select Item", JOptionPane.INFORMATION_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Please select an item to toggle availability.", "Select Item", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        MenuItem item = menuItemTableModel.getItemAt(selectedRow);
        boolean newState = !item.available();
        String actionStr = newState ? "make available" : "mark 86'd (unavailable)";

        int choice = JOptionPane.showConfirmDialog(this,
                "Do you want to " + actionStr + " for '" + item.name() + "'?",
                "Confirm Availability Change", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                context.menuService().setAvailability(item.id(), newState, currentUser.id(), currentUser.username());
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error updating availability: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
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
                case 5 -> item.available() ? "Available" : "86'd";
                default -> "";
            };
        }
    }

    private static final class RoundedPillAvailabilityRenderer extends DefaultTableCellRenderer {
        private String currentStatus = "";
        private boolean isRowSelected = false;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            this.currentStatus = value != null ? value.toString() : "";
            this.isRowSelected = isSelected;
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            setOpaque(false);

            if (currentStatus.startsWith("Available")) {
                setForeground(AppTheme.SUCCESS);
            } else {
                setForeground(AppTheme.DANGER);
            }
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int pillW = 80;
            int pillH = 22;
            int x = (w - pillW) / 2;
            int y = (h - pillH) / 2;

            if (currentStatus.startsWith("Available")) {
                g2.setColor(isRowSelected ? Color.decode("#BBF7D0") : AppTheme.SUCCESS_BG);
                g2.fillRoundRect(x, y, pillW, pillH, 12, 12);
                g2.setColor(AppTheme.SUCCESS_BORDER);
                g2.drawRoundRect(x, y, pillW, pillH, 12, 12);
            } else {
                g2.setColor(isRowSelected ? Color.decode("#FECACA") : AppTheme.DANGER_BG);
                g2.fillRoundRect(x, y, pillW, pillH, 12, 12);
                g2.setColor(AppTheme.DANGER_BORDER);
                g2.drawRoundRect(x, y, pillW, pillH, 12, 12);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
