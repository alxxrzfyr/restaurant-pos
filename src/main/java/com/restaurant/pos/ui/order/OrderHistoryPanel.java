package com.restaurant.pos.ui.order;

import com.restaurant.pos.AppContext;
import com.restaurant.pos.model.Order;
import com.restaurant.pos.model.OrderStatus;
import com.restaurant.pos.model.Payment;
import com.restaurant.pos.model.Role;
import com.restaurant.pos.model.User;
import com.restaurant.pos.model.CheckoutResult;
import com.restaurant.pos.ui.components.DatePickerButton;
import com.restaurant.pos.ui.components.StripedTableCellRenderer;
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
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Frame;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class OrderHistoryPanel extends JPanel {

    private static final int BUTTON_HEIGHT = 44;
    private static final int INPUT_HEIGHT = 38;
    private static final int ROW_HEIGHT = 36;
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private final AppContext context;
    private final User currentUser;
    private final OrderTableModel tableModel = new OrderTableModel();
    private final DatePickerButton fromPicker = new DatePickerButton(LocalDate.now());
    private final DatePickerButton toPicker   = new DatePickerButton(LocalDate.now());
    private final JTable orderTable;

    public OrderHistoryPanel(AppContext context) {
        this(context, null);
    }

    public OrderHistoryPanel(AppContext context, User currentUser) {
        super(new BorderLayout(0, 12));
        this.context = context;
        this.currentUser = currentUser;

        setBackground(AppTheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(buildFilterBar(), BorderLayout.NORTH);

        orderTable = new JTable(tableModel);
        orderTable.setRowHeight(ROW_HEIGHT);
        orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        orderTable.getTableHeader().setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_TABLE_HEADER));
        orderTable.setFont(AppTheme.bodyFont());
        StripedTableCellRenderer.apply(orderTable);

        orderTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        orderTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        orderTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        orderTable.getColumnModel().getColumn(3).setPreferredWidth(110);
        orderTable.getColumnModel().getColumn(4).setPreferredWidth(80);

        JScrollPane scrollPane = new JScrollPane(orderTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER));
        add(scrollPane, BorderLayout.CENTER);

        add(buildActionBar(), BorderLayout.SOUTH);

        loadToday();
    }

    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new MigLayout("insets 0", "[]16[]8[]16[]8[]16[]push"));
        bar.setOpaque(false);

        JLabel title = new JLabel("Order History");
        title.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_PAGE_TITLE));
        bar.add(title);

        JLabel fromLabel = new JLabel("From:");
        fromLabel.setFont(AppTheme.bodyFont());
        bar.add(fromLabel);
        bar.add(fromPicker, "w 175!, h " + INPUT_HEIGHT + "!");

        JLabel toLabel = new JLabel("To:");
        toLabel.setFont(AppTheme.bodyFont());
        bar.add(toLabel);
        bar.add(toPicker, "w 175!, h " + INPUT_HEIGHT + "!");

        JButton searchBtn = new JButton("Search");
        searchBtn.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_TABLE_HEADER));
        searchBtn.setIcon(Icons.search(Color.WHITE, 16));
        searchBtn.setBackground(AppTheme.PRIMARY);
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchBtn.addActionListener(e -> loadByDateRange());
        bar.add(searchBtn, "h " + BUTTON_HEIGHT + "!, w 110!");

        return bar;
    }

    private JPanel buildActionBar() {
        JPanel bar = new JPanel(new MigLayout("insets 8 0 0 0", "push[]12[]"));
        bar.setOpaque(false);

        if (currentUser != null && currentUser.role() == Role.ADMINISTRATOR) {
            JButton voidBtn = new JButton("Void Order");
            voidBtn.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_TABLE_HEADER));
            voidBtn.setIcon(Icons.xCircle(Color.WHITE, 16));
            voidBtn.setBackground(AppTheme.DANGER);
            voidBtn.setForeground(Color.WHITE);
            voidBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            voidBtn.addActionListener(e -> voidSelectedOrder());
            bar.add(voidBtn, "h " + BUTTON_HEIGHT + "!, w 140!");
        }

        JButton reprintBtn = new JButton("Reprint Receipt");
        reprintBtn.setFont(AppTheme.titleFont(AppTheme.FONT_SIZE_TABLE_HEADER));
        reprintBtn.setIcon(Icons.printer(Color.WHITE, 16));
        reprintBtn.setBackground(AppTheme.PRIMARY);
        reprintBtn.setForeground(Color.WHITE);
        reprintBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        reprintBtn.addActionListener(e -> reprintSelected());
        bar.add(reprintBtn, "h " + BUTTON_HEIGHT + "!, w 160!");

        return bar;
    }

    private void loadToday() {
        LocalDate today = LocalDate.now();
        Instant from = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant to = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        tableModel.setOrders(context.orderService().findOrderHistory(from, to));
    }

    private void loadByDateRange() {
        LocalDate fromDate = fromPicker.getSelectedDate();
        LocalDate toDate   = toPicker.getSelectedDate();
        Instant from = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant to   = toDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        tableModel.setOrders(context.orderService().findOrderHistory(from, to));
    }

    private void reprintSelected() {
        int row = orderTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an order to reprint.",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Order order = tableModel.getOrderAt(row);
        Optional<Order> fullOrder = context.orderService().findOrderById(order.id());
        Optional<Payment> payment = context.orderService().findPaymentForOrder(order.id());

        if (fullOrder.isEmpty() || payment.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Could not load order details for reprinting.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        CheckoutResult result = new CheckoutResult(fullOrder.get(), payment.get());
        new ReceiptDialog(owner, context, result).setVisible(true);
    }

    private void voidSelectedOrder() {
        int row = orderTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an order to void.",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Order order = tableModel.getOrderAt(row);
        if (order.status() == OrderStatus.VOIDED) {
            JOptionPane.showMessageDialog(this, "Order #" + order.orderNumber() + " is already voided.",
                    "Already Voided", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        ConfirmVoidDialog dialog = new ConfirmVoidDialog(owner, context, currentUser, order.orderNumber());
        dialog.setVisible(true);

        String reason = dialog.getConfirmedReason();
        if (reason != null) {
            try {
                context.orderService().voidOrder(order.id(), currentUser.id(), currentUser.username(), reason);
                loadByDateRange();
                JOptionPane.showMessageDialog(this, "Order #" + order.orderNumber() + " has been voided.",
                        "Order Voided", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to void order: " + ex.getMessage(),
                        "Void Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private final class OrderTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {"Order #", "Date/Time", "Cashier", "Total", "Status"};
        private List<Order> orders = new ArrayList<>();

        void setOrders(List<Order> orders) {
            this.orders = new ArrayList<>(orders);
            fireTableDataChanged();
        }

        Order getOrderAt(int row) {
            return orders.get(row);
        }

        @Override
        public int getRowCount() { return orders.size(); }

        @Override
        public int getColumnCount() { return COLUMNS.length; }

        @Override
        public String getColumnName(int column) { return COLUMNS[column]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Order o = orders.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> o.orderNumber();
                case 1 -> TIME_FORMAT.format(o.placedAt());
                case 2 -> o.cashierName();
                case 3 -> MoneyFormatter.format(o.totalDue());
                case 4 -> o.status().name();
                default -> "";
            };
        }
    }
}
