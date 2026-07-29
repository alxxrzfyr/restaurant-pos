package com.restaurant.pos.testutil;

import com.restaurant.pos.model.Category;
import com.restaurant.pos.repository.CategoryRepository;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/** In-memory {@link CategoryRepository} fake for service-layer unit tests. */
public final class InMemoryCategoryRepository implements CategoryRepository {

    private final Map<Long, Category> categoriesById = new LinkedHashMap<>();
    private final AtomicLong idSequence = new AtomicLong();

    @Override
    public Optional<Category> findById(long id) {
        return Optional.ofNullable(categoriesById.get(id));
    }

    @Override
    public List<Category> findAllOrdered() {
        return categoriesById.values().stream().sorted(Comparator.comparingInt(Category::displayOrder)).toList();
    }

    @Override
    public Category insert(Category category) {
        Category saved = category.toBuilder().id(idSequence.incrementAndGet()).build();
        categoriesById.put(saved.id(), saved);
        return saved;
    }

    @Override
    public void update(Category category) {
        categoriesById.put(category.id(), category);
    }

    @Override
    public void delete(long id) {
        categoriesById.remove(id);
    }
}
