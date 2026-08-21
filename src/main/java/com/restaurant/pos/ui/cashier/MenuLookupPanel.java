package com.restaurant.pos.ui.cashier;

import com.restaurant.pos.AppContext;
import com.restaurant.pos.model.Category;
import com.restaurant.pos.model.MenuItem;
import com.restaurant.pos.ui.components.StripedTableCellRenderer;
import com.restaurant.pos.ui.format.MoneyFormatter;
import com.restaurant.pos.ui.theme.AppTheme;
import com.restaurant.pos.ui.theme.Icons;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class MenuLookupPanel extends JPanel {

    private static final int ROW_HEIGHT = 36;

    private final AppContext context;

    private final JTextField searchField = new JTextField(20);
    private final JComboBox<String> categoryFilter = new JComboBox<>();
    private final MenuItemTableModel tableModel = new MenuItemTableModel();
    private final JTable table = new JTable(tableModel);

    private List<MenuItem> allAvailableItems = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();

    public MenuLookupPanel(AppContext context) {
        super(new BorderLayout(0, 12));
        this.context = context;

        setBackground(AppTheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(buildHeaderBar(), BorderLayout.NORTH);
        add(buildTablePane(), BorderLayout.CENTER);

        loadData();
    }

    private JPanel buildHeaderBar() {
        JPanel header = new JPanel(new MigLayout("insets 0, fillx", "[grow][]"));
        header.setOpaque(false);

        JLabel title = new JLabel("Menu Lookup");
        title.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_PAGE_TITLE));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        header.add(title);

        JPanel controls = new JPanel(new MigLayout("insets 0", "[][][]"));
        controls.setOpaque(false);

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(AppTheme.bodyFont());
        searchLabel.setForeground(AppTheme.TEXT_SECONDARY);

        searchField.setFont(AppTheme.bodyFont());
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filter(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filter(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filter(); }
        });

        JLabel catLabel = new JLabel("Category:");
        catLabel.setFont(AppTheme.bodyFont());
        catLabel.setForeground(AppTheme.TEXT_SECONDARY);

        categoryFilter.setFont(AppTheme.bodyFont());
        categoryFilter.addActionListener(e -> filter());

        controls.add(searchLabel);
        controls.add(searchField, "h 36!, w 200!");
        controls.add(catLabel, "gapleft 14");
        controls.add(categoryFilter, "h 36!, w 180!");

        header.add(controls);
        return header;
    }

    private JPanel buildTablePane() {
        JPanel container = new JPanel(new BorderLayout(0, 8));
        container.setBackground(AppTheme.CARD);
        container.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        table.setRowHeight(ROW_HEIGHT);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_TABLE_HEADER));
        table.setFont(AppTheme.bodyFont());
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < 3; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        container.add(new JScrollPane(table), BorderLayout.CENTER);
        return container;
    }

    private void loadData() {
        allAvailableItems = context.menuService().findAllAvailable();
        categories = context.categoryService().findAllOrdered();

        categoryFilter.removeAllItems();
        categoryFilter.addItem("All Categories");
        for (Category cat : categories) {
            categoryFilter.addItem(cat.name());
        }

        filter();
    }

    private void filter() {
        String query = searchField.getText().trim().toLowerCase();
        String selectedCategory = (String) categoryFilter.getSelectedItem();

        List<MenuItem> filtered = allAvailableItems.stream()
                .filter(item -> {
                    boolean matchesSearch = query.isEmpty() || item.name().toLowerCase().contains(query);
                    boolean matchesCat = selectedCategory == null || "All Categories".equals(selectedCategory)
                            || Objects.equals(item.categoryName(), selectedCategory);
                    return matchesSearch && matchesCat;
                })
                .toList();

        tableModel.setItems(filtered);
    }

    private static final class MenuItemTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {"Item Name", "Category", "Price"};
        private List<MenuItem> items = new ArrayList<>();

        void setItems(List<MenuItem> items) {
            this.items = new ArrayList<>(items);
            fireTableDataChanged();
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
                case 0 -> item.name();
                case 1 -> item.categoryName() != null ? item.categoryName() : item.categoryId();
                case 2 -> MoneyFormatter.format(item.price());
                default -> "";
            };
        }
    }
}
