package com.restaurant.pos.repository.sqlite;

import com.restaurant.pos.database.ConnectionManager;
import com.restaurant.pos.model.Money;
import com.restaurant.pos.model.MenuItem;
import com.restaurant.pos.repository.MenuItemRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class SqliteMenuItemRepository implements MenuItemRepository {

    private static final String SELECT_WITH_CATEGORY =
            "SELECT m.*, c.name AS category_name FROM menu_items m JOIN categories c ON m.category_id = c.id";

    private final ConnectionManager connectionManager;

    public SqliteMenuItemRepository(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Optional<MenuItem> findById(long id) {
        return connectionManager.execute(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(SELECT_WITH_CATEGORY + " WHERE m.id = ?")) {
                stmt.setLong(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
                }
            }
        });
    }

    @Override
    public List<MenuItem> findAll() {
        return queryList(SELECT_WITH_CATEGORY + " ORDER BY c.display_order, m.name");
    }

    @Override
    public List<MenuItem> findAllAvailable() {
        return queryList(SELECT_WITH_CATEGORY + " WHERE m.available = 1 ORDER BY c.display_order, m.name");
    }

    @Override
    public List<MenuItem> findByCategory(long categoryId) {
        return connectionManager.execute(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    SELECT_WITH_CATEGORY + " WHERE m.category_id = ? ORDER BY m.name")) {
                stmt.setLong(1, categoryId);
                try (ResultSet rs = stmt.executeQuery()) {
                    List<MenuItem> items = new ArrayList<>();
                    while (rs.next()) {
                        items.add(mapRow(rs));
                    }
                    return items;
                }
            }
        });
    }

    @Override
    public MenuItem insert(MenuItem item) {
        return connectionManager.execute(connection -> {
            long now = System.currentTimeMillis();
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO menu_items (name, price_minor, cost_minor, category_id, available, image_path, "
                            + "created_at_epoch_ms, updated_at_epoch_ms) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, item.name());
                stmt.setLong(2, item.price().toMinorUnits());
                JdbcUtil.setNullableLong(stmt, 3, item.cost() == null ? null : item.cost().toMinorUnits());
                stmt.setLong(4, item.categoryId());
                stmt.setInt(5, item.available() ? 1 : 0);
                JdbcUtil.setNullableString(stmt, 6, item.imagePath());
                stmt.setLong(7, now);
                stmt.setLong(8, now);
                stmt.executeUpdate();

                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    keys.next();
                    return item.toBuilder().id(keys.getLong(1)).build();
                }
            }
        });
    }

    @Override
    public void update(MenuItem item) {
        connectionManager.execute(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE menu_items SET name = ?, price_minor = ?, cost_minor = ?, category_id = ?, "
                            + "available = ?, image_path = ?, updated_at_epoch_ms = ? WHERE id = ?")) {
                stmt.setString(1, item.name());
                stmt.setLong(2, item.price().toMinorUnits());
                JdbcUtil.setNullableLong(stmt, 3, item.cost() == null ? null : item.cost().toMinorUnits());
                stmt.setLong(4, item.categoryId());
                stmt.setInt(5, item.available() ? 1 : 0);
                JdbcUtil.setNullableString(stmt, 6, item.imagePath());
                stmt.setLong(7, System.currentTimeMillis());
                stmt.setLong(8, item.id());
                return stmt.executeUpdate();
            }
        });
    }

    @Override
    public void setAvailability(long id, boolean available) {
        connectionManager.execute(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE menu_items SET available = ?, updated_at_epoch_ms = ? WHERE id = ?")) {
                stmt.setInt(1, available ? 1 : 0);
                stmt.setLong(2, System.currentTimeMillis());
                stmt.setLong(3, id);
                return stmt.executeUpdate();
            }
        });
    }

    private List<MenuItem> queryList(String sql) {
        return connectionManager.execute(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                List<MenuItem> items = new ArrayList<>();
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
                return items;
            }
        });
    }

    private static MenuItem mapRow(ResultSet rs) throws SQLException {
        Long costMinor = JdbcUtil.getNullableLong(rs, "cost_minor");
        return MenuItem.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .price(Money.ofMinorUnits(rs.getLong("price_minor")))
                .cost(costMinor == null ? null : Money.ofMinorUnits(costMinor))
                .categoryId(rs.getLong("category_id"))
                .categoryName(rs.getString("category_name"))
                .available(rs.getInt("available") == 1)
                .imagePath(rs.getString("image_path"))
                .build();
    }
}
