package com.restaurant.pos.ui.order;

import com.restaurant.pos.AppContext;
import com.restaurant.pos.model.MenuItem;
import com.restaurant.pos.model.Order;
import com.restaurant.pos.ui.components.StripedTableCellRenderer;
import com.restaurant.pos.ui.format.MoneyFormatter;
import com.restaurant.pos.ui.theme.AppTheme;
import com.restaurant.pos.ui.theme.Icons;
import net.miginfocom.swing.MigLayout;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class SearchDialog extends JDialog {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private final AppContext context;

    private final JTextField searchField = new JTextField();
    private final DefaultTableModel menuTableModel;
    private final JTable menuTable;
    private final DefaultTableModel orderTableModel;
    private final JTable orderTable;

    private List<MenuItem> allMenuItems = new ArrayList<>();
    private List<Order> allOrders = new ArrayList<>();

    public SearchDialog(Frame owner, AppContext context) {
        super(owner, "Global Search", true);
        this.context = context;

        menuTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Category", "Price", "Available"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        menuTable = new JTable(menuTableModel);

        orderTableModel = new DefaultTableModel(new String[]{"Order #", "Date / Time", "Cashier", "Total", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        orderTable = new JTable(orderTableModel);

        setContentPane(buildContent());
        pack();
        setLocationRelativeTo(owner);
        setResizable(false);

        setupEscapeKey();
        loadData();
    }

    private JPanel buildContent() {
        JPanel panel = new JPanel(new MigLayout("insets 20, wrap 1, fill", "[640!]"));
        panel.setBackground(AppTheme.CARD);

        JPanel headerPanel = new JPanel(new MigLayout("insets 0", "[]10[grow]"));
        headerPanel.setOpaque(false);

        JLabel searchIcon = new JLabel(Icons.search(AppTheme.ACCENT, 20));
        headerPanel.add(searchIcon);

        searchField.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_PAGE_TITLE));
        searchField.setToolTipText("Type to filter menu items or orders...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterData(); }
            @Override public void removeUpdate(DocumentEvent e) { filterData(); }
            @Override public void changedUpdate(DocumentEvent e) { filterData(); }
        });
        headerPanel.add(searchField, "growx, h 40!");

        panel.add(headerPanel, "growx, gapbottom 12");

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_BODY));

        menuTable.setFont(AppTheme.bodyFont());
        menuTable.setRowHeight(32);
        menuTable.getTableHeader().setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_TABLE_HEADER));
        menuTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        StripedTableCellRenderer.apply(menuTable);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < 5; i++) {
            menuTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane menuScroll = new JScrollPane(menuTable);
        menuScroll.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER));
        tabbedPane.addTab("Menu Items", Icons.menu(AppTheme.TEXT_SECONDARY, 14), menuScroll);

        orderTable.setFont(AppTheme.bodyFont());
        orderTable.setRowHeight(32);
        orderTable.getTableHeader().setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_TABLE_HEADER));
        orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        StripedTableCellRenderer.apply(orderTable);

        for (int i = 0; i < 5; i++) {
            orderTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        orderTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && orderTable.getSelectedRow() >= 0) {
                    openSelectedOrderReceipt();
                }
            }
        });
        JScrollPane orderScroll = new JScrollPane(orderTable);
        orderScroll.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER));

        JPanel orderPanel = new JPanel(new MigLayout("insets 0, wrap 1, fill", "[grow, fill]"));
        orderPanel.setOpaque(false);
        orderPanel.add(orderScroll, "grow, push, h 340!");

        JButton reprintBtn = new JButton("View Receipt");
        reprintBtn.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_BODY));
        reprintBtn.setIcon(Icons.fileText(AppTheme.TEXT_PRIMARY, 14));
        reprintBtn.setIconTextGap(6);
        reprintBtn.setBackground(AppTheme.CARD);
        reprintBtn.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER));
        reprintBtn.setFocusPainted(false);
        reprintBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        reprintBtn.addActionListener(e -> openSelectedOrderReceipt());
        orderPanel.add(reprintBtn, "align right, gapy 8");

        tabbedPane.addTab("Orders", Icons.orders(AppTheme.TEXT_SECONDARY, 14), orderPanel);

        panel.add(tabbedPane, "grow, h 400!, gapbottom 12");

        JButton closeBtn = new JButton("Close (Esc)");
        closeBtn.setFont(AppTheme.bodyFont());
        closeBtn.setBackground(AppTheme.CARD);
        closeBtn.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER));
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());
        panel.add(closeBtn, "align right, h 36!");

        return panel;
    }

    private void loadData() {
        try {
            allMenuItems = context.menuService().findAll();
            allOrders = context.orderService().findOrderHistory(null, null);
            filterData();
        } catch (Exception ignored) {
        }
    }

    private void filterData() {
        String query = searchField.getText().trim().toLowerCase();

        menuTableModel.setRowCount(0);
        for (MenuItem item : allMenuItems) {
            if (query.isEmpty() ||
                    item.name().toLowerCase().contains(query) ||
                    item.categoryName().toLowerCase().contains(query)) {
                menuTableModel.addRow(new Object[]{
                        item.id(),
                        item.name(),
                        item.categoryName(),
                        MoneyFormatter.format(item.price()),
                        item.available() ? "Available" : "86'd"
                });
            }
        }

        orderTableModel.setRowCount(0);
        for (Order order : allOrders) {
            String dateStr = TIMESTAMP_FORMAT.format(order.placedAt());
            if (query.isEmpty() ||
                    order.orderNumber().toLowerCase().contains(query) ||
                    order.cashierName().toLowerCase().contains(query) ||
                    dateStr.toLowerCase().contains(query)) {
                orderTableModel.addRow(new Object[]{
                        order.orderNumber(),
                        dateStr,
                        order.cashierName(),
                        MoneyFormatter.format(order.totalDue()),
                        order.status().name()
                });
            }
        }
    }

    private void openSelectedOrderReceipt() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow < 0) return;

        String orderNum = (String) orderTableModel.getValueAt(selectedRow, 0);

        try {
            long orderId = Long.parseLong(orderNum);
            Order fullOrder = context.orderService().findOrderById(orderId).orElse(null);
            var payment = context.orderService().findPaymentForOrder(orderId).orElse(null);

            if (fullOrder != null && payment != null) {
                ReceiptDialog receiptDialog = new ReceiptDialog((Frame) getOwner(), context, fullOrder, payment);
                receiptDialog.setVisible(true);
            }
        } catch (NumberFormatException ignored) {}
    }

    private void setupEscapeKey() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESCAPE_KEY");
        getRootPane().getActionMap().put("ESCAPE_KEY", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
}
