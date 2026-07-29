package com.restaurant.pos.repository.sqlite;

import com.restaurant.pos.database.ConnectionManager;
import com.restaurant.pos.model.Category;
import com.restaurant.pos.repository.CategoryRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class SqliteCategoryRepository implements CategoryRepository {

    private final ConnectionManager connectionManager;

    public SqliteCategoryRepository(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Optional<Category> findById(long id) {
        return connectionManager.execute(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT * FROM categories WHERE id = ?")) {
                stmt.setLong(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
                }
            }
        });
    }

    @Override
    public List<Category> findAllOrdered() {
        return connectionManager.execute(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM categories ORDER BY display_order");
                 ResultSet rs = stmt.executeQuery()) {
                List<Category> categories = new ArrayList<>();
                while (rs.next()) {
                    categories.add(mapRow(rs));
                }
                return categories;
            }
        });
    }

    @Override
    public Category insert(Category category) {
        return connectionManager.execute(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO categories (name, display_order) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, category.name());
                stmt.setInt(2, category.displayOrder());
                stmt.executeUpdate();

                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    keys.next();
                    return category.toBuilder().id(keys.getLong(1)).build();
                }
            }
        });
    }

    @Override
    public void update(Category category) {
        connectionManager.execute(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE categories SET name = ?, display_order = ? WHERE id = ?")) {
                stmt.setString(1, category.name());
                stmt.setInt(2, category.displayOrder());
                stmt.setLong(3, category.id());
                return stmt.executeUpdate();
            }
        });
    }

    @Override
    public void delete(long id) {
        connectionManager.execute(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM categories WHERE id = ?")) {
                stmt.setLong(1, id);
                return stmt.executeUpdate();
            }
        });
    }

    private static Category mapRow(ResultSet rs) throws SQLException {
        return Category.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .displayOrder(rs.getInt("display_order"))
                .build();
    }
}
