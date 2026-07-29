package com.restaurant.pos.service;

import com.restaurant.pos.exception.CategoryInUseException;
import com.restaurant.pos.model.AuditEventType;
import com.restaurant.pos.model.Category;
import com.restaurant.pos.model.MenuItem;
import com.restaurant.pos.repository.CategoryRepository;
import com.restaurant.pos.repository.MenuItemRepository;

import java.util.List;

public final class CategoryService {

    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final AuthService authService;
    private final AuditService auditService;

    public CategoryService(CategoryRepository categoryRepository, MenuItemRepository menuItemRepository,
                           AuthService authService, AuditService auditService) {
        this.categoryRepository = categoryRepository;
        this.menuItemRepository = menuItemRepository;
        this.authService = authService;
        this.auditService = auditService;
    }

    public List<Category> findAllOrdered() {
        return categoryRepository.findAllOrdered();
    }

    public Category create(String name, int displayOrder, long actingUserId, String actingUsername) {
        authService.requireAdmin(actingUserId);
        Category category = categoryRepository.insert(Category.builder().name(name).displayOrder(displayOrder).build());
        auditService.record(AuditEventType.CATEGORY_CREATED, actingUserId, actingUsername, category.name());
        return category;
    }

    public void update(Category category, long actingUserId, String actingUsername) {
        authService.requireAdmin(actingUserId);
        categoryRepository.update(category);
        auditService.record(AuditEventType.CATEGORY_UPDATED, actingUserId, actingUsername, category.name());
    }

    public void delete(long categoryId, long actingUserId, String actingUsername) {
        authService.requireAdmin(actingUserId);
        List<MenuItem> itemsInCategory = menuItemRepository.findByCategory(categoryId);
        if (!itemsInCategory.isEmpty()) {
            throw new CategoryInUseException(itemsInCategory.size());
        }
        categoryRepository.delete(categoryId);
        auditService.record(AuditEventType.CATEGORY_DELETED, actingUserId, actingUsername, "id=" + categoryId);
    }
}
