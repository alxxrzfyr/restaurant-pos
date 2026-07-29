package com.restaurant.pos.repository.sqlite;

import com.restaurant.pos.database.ConnectionManager;
import com.restaurant.pos.model.CheckoutResult;
import com.restaurant.pos.model.Money;
import com.restaurant.pos.model.Order;
import com.restaurant.pos.model.OrderLineItem;
import com.restaurant.pos.model.OrderStatus;
import com.restaurant.pos.model.OrderType;
import com.restaurant.pos.model.Payment;
import com.restaurant.pos.repository.OrderRepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class SqliteOrderRepository implements OrderRepository {

    private static final String SELECT_ORDER_WITH_CASHIER =
            "SELECT o.*, u.display_name AS cashier_name FROM orders o JOIN users u ON o.cashier_id = u.id";

    private final ConnectionManager connectionManager;

    public SqliteOrderRepository(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Optional<Order> findById(long id) {
        return connectionManager.execute(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(SELECT_ORDER_WITH_CASHIER + " WHERE o.id = ?")) {
                stmt.setLong(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        return Optional.empty();
                    }
                    return Optional.of(mapOrder(rs, findLineItems(connection, id)));
                }
            }
        });
    }

    @Override
    public CheckoutResult placeOrder(Order order, Payment payment) {
        return connectionManager.inTransaction(connection -> {
            long orderId = insertOrder(connection, order);
            insertLineItems(connection, orderId, order.lineItems());
            long paymentId = insertPayment(connection, orderId, payment);

            Order savedOrder = order.toBuilder().id(orderId).build();
            Payment savedPayment = payment.toBuilder().id(paymentId).orderId(orderId).build();
            return new CheckoutResult(savedOrder, savedPayment);
        });
    }

    @Override
    public void markVoided(long orderId, long voidedByUserId, String reason, Instant voidedAt) {
        connectionManager.execute(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE orders SET status = ?, voided_at_epoch_ms = ?, voided_by_user_id = ?, void_reason = ? "
                            + "WHERE id = ?")) {
                stmt.setString(1, OrderStatus.VOIDED.name());
                stmt.setLong(2, voidedAt.toEpochMilli());
                stmt.setLong(3, voidedByUserId);
                stmt.setString(4, reason);
                stmt.setLong(5, orderId);
                return stmt.executeUpdate();
            }
        });
    }

    @Override
    public List<Order> findByDateRange(Instant from, Instant to) {
        return queryOrders(SELECT_ORDER_WITH_CASHIER
                + " WHERE o.placed_at_epoch_ms >= ? AND o.placed_at_epoch_ms < ? ORDER BY o.placed_at_epoch_ms DESC",
                from, to);
    }

    @Override
    public List<Order> findByCashierAndDateRange(long cashierId, Instant from, Instant to) {
        return connectionManager.execute(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(SELECT_ORDER_WITH_CASHIER
                    + " WHERE o.cashier_id = ? AND o.placed_at_epoch_ms >= ? AND o.placed_at_epoch_ms < ? "
                    + "ORDER BY o.placed_at_epoch_ms DESC")) {
                stmt.setLong(1, cashierId);
                stmt.setLong(2, from.toEpochMilli());
                stmt.setLong(3, to.toEpochMilli());
                return mapOrderList(connection, stmt);
            }
        });
    }

    @Override
    public List<Order> findByStatusAndDateRange(OrderStatus status, Instant from, Instant to) {
        return connectionManager.execute(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(SELECT_ORDER_WITH_CASHIER
                    + " WHERE o.status = ? AND o.placed_at_epoch_ms >= ? AND o.placed_at_epoch_ms < ? "
                    + "ORDER BY o.placed_at_epoch_ms DESC")) {
                stmt.setString(1, status.name());
                stmt.setLong(2, from.toEpochMilli());
                stmt.setLong(3, to.toEpochMilli());
                return mapOrderList(connection, stmt);
            }
        });
    }

    private List<Order> queryOrders(String sql, Instant from, Instant to) {
        return connectionManager.execute(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, from.toEpochMilli());
                stmt.setLong(2, to.toEpochMilli());
                return mapOrderList(connection, stmt);
            }
        });
    }

    private List<Order> mapOrderList(Connection connection, PreparedStatement stmt) throws SQLException {
        List<Order> orders = new ArrayList<>();
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                long orderId = rs.getLong("id");
                orders.add(mapOrder(rs, findLineItems(connection, orderId)));
            }
        }
        return orders;
    }

    private long insertOrder(Connection connection, Order order) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO orders (cashier_id, order_type, table_number, notes, subtotal_minor, "
                        + "discount_minor, vat_rate_percent, vat_minor, total_due_minor, status, placed_at_epoch_ms) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, order.cashierId());
            stmt.setString(2, order.orderType().name());
            JdbcUtil.setNullableString(stmt, 3, order.tableNumber());
            JdbcUtil.setNullableString(stmt, 4, order.notes());
            stmt.setLong(5, order.subtotal().toMinorUnits());
            stmt.setLong(6, order.discountAmount().toMinorUnits());
            stmt.setString(7, order.vatRatePercent().toPlainString());
            stmt.setLong(8, order.vatAmount().toMinorUnits());
            stmt.setLong(9, order.totalDue().toMinorUnits());
            stmt.setString(10, OrderStatus.PAID.name());
            stmt.setLong(11, order.placedAt().toEpochMilli());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    private void insertLineItems(Connection connection, long orderId, List<OrderLineItem> lineItems) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO order_line_items (order_id, menu_item_id, item_name, unit_price_minor, quantity) "
                        + "VALUES (?, ?, ?, ?, ?)")) {
            for (OrderLineItem line : lineItems) {
                stmt.setLong(1, orderId);
                JdbcUtil.setNullableLong(stmt, 2, line.menuItemId());
                stmt.setString(3, line.itemName());
                stmt.setLong(4, line.unitPrice().toMinorUnits());
                stmt.setInt(5, line.quantity());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private long insertPayment(Connection connection, long orderId, Payment payment) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO payments (order_id, method, amount_tendered_minor, change_given_minor, paid_at_epoch_ms) "
                        + "VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, orderId);
            stmt.setString(2, payment.method().name());
            stmt.setLong(3, payment.amountTendered().toMinorUnits());
            stmt.setLong(4, payment.changeGiven().toMinorUnits());
            stmt.setLong(5, payment.paidAt().toEpochMilli());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    private List<OrderLineItem> findLineItems(Connection connection, long orderId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT * FROM order_line_items WHERE order_id = ?")) {
            stmt.setLong(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<OrderLineItem> lines = new ArrayList<>();
                while (rs.next()) {
                    lines.add(OrderLineItem.builder()
                            .id(rs.getLong("id"))
                            .menuItemId(JdbcUtil.getNullableLong(rs, "menu_item_id"))
                            .itemName(rs.getString("item_name"))
                            .unitPrice(Money.ofMinorUnits(rs.getLong("unit_price_minor")))
                            .quantity(rs.getInt("quantity"))
                            .build());
                }
                return lines;
            }
        }
    }

    private static Order mapOrder(ResultSet rs, List<OrderLineItem> lineItems) throws SQLException {
        return Order.builder()
                .id(rs.getLong("id"))
                .cashierId(rs.getLong("cashier_id"))
                .cashierName(rs.getString("cashier_name"))
                .orderType(OrderType.valueOf(rs.getString("order_type")))
                .tableNumber(rs.getString("table_number"))
                .notes(rs.getString("notes"))
                .lineItems(lineItems)
                .subtotal(Money.ofMinorUnits(rs.getLong("subtotal_minor")))
                .discountAmount(Money.ofMinorUnits(rs.getLong("discount_minor")))
                .vatRatePercent(new BigDecimal(rs.getString("vat_rate_percent")))
                .vatAmount(Money.ofMinorUnits(rs.getLong("vat_minor")))
                .totalDue(Money.ofMinorUnits(rs.getLong("total_due_minor")))
                .status(OrderStatus.valueOf(rs.getString("status")))
                .placedAt(Instant.ofEpochMilli(rs.getLong("placed_at_epoch_ms")))
                .voidedAt(JdbcUtil.getNullableInstant(rs, "voided_at_epoch_ms"))
                .voidedByUserId(JdbcUtil.getNullableLong(rs, "voided_by_user_id"))
                .voidReason(rs.getString("void_reason"))
                .build();
    }
}
