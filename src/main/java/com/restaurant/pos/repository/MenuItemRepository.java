package com.restaurant.pos.repository;

import com.restaurant.pos.model.MenuItem;

import java.util.List;
import java.util.Optional;

public interface MenuItemRepository {

    Optional<MenuItem> findById(long id);

    List<MenuItem> findAll();

    List<MenuItem> findAllAvailable();

    List<MenuItem> findByCategory(long categoryId);

    MenuItem insert(MenuItem item);

    void update(MenuItem item);

    void setAvailability(long id, boolean available);
}
