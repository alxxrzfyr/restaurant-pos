package com.restaurant.pos.repository;

import com.restaurant.pos.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {

    Optional<Category> findById(long id);

    List<Category> findAllOrdered();

    Category insert(Category category);

    void update(Category category);

    void delete(long id);
}
