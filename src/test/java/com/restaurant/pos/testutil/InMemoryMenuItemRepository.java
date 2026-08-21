package com.restaurant.pos.testutil;

import com.restaurant.pos.model.MenuItem;
import com.restaurant.pos.repository.MenuItemRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public final class InMemoryMenuItemRepository implements MenuItemRepository {

    private final Map<Long, MenuItem> itemsById = new LinkedHashMap<>();
    private final AtomicLong idSequence = new AtomicLong();

    @Override
    public Optional<MenuItem> findById(long id) {
        return Optional.ofNullable(itemsById.get(id));
    }

    @Override
    public List<MenuItem> findAll() {
        return List.copyOf(itemsById.values());
    }

    @Override
    public List<MenuItem> findAllAvailable() {
        return itemsById.values().stream().filter(MenuItem::available).toList();
    }

    @Override
    public List<MenuItem> findByCategory(long categoryId) {
        return itemsById.values().stream().filter(item -> item.categoryId() == categoryId).toList();
    }

    @Override
    public MenuItem insert(MenuItem item) {
        MenuItem saved = item.toBuilder().id(idSequence.incrementAndGet()).build();
        itemsById.put(saved.id(), saved);
        return saved;
    }

    @Override
    public void update(MenuItem item) {
        itemsById.put(item.id(), item);
    }

    @Override
    public void setAvailability(long id, boolean available) {
        MenuItem existing = itemsById.get(id);
        itemsById.put(id, existing.toBuilder().available(available).build());
    }
}
