package com.restaurant.pos.service;

import com.restaurant.pos.exception.CategoryInUseException;
import com.restaurant.pos.model.Category;
import com.restaurant.pos.model.Money;
import com.restaurant.pos.model.MenuItem;
import com.restaurant.pos.testutil.InMemoryAuditEventRepository;
import com.restaurant.pos.testutil.InMemoryCategoryRepository;
import com.restaurant.pos.testutil.InMemoryMenuItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CategoryServiceTest {

    private InMemoryCategoryRepository categoryRepository;
    private InMemoryMenuItemRepository menuItemRepository;
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryRepository = new InMemoryCategoryRepository();
        menuItemRepository = new InMemoryMenuItemRepository();
        com.restaurant.pos.repository.UserRepository userRepository = new com.restaurant.pos.testutil.InMemoryUserRepository();
        userRepository.insert(com.restaurant.pos.model.User.builder().id(1L).username("admin").displayName("Admin").role(com.restaurant.pos.model.Role.ADMINISTRATOR).passwordHash("hash").active(true).build());
        AuditService auditService = new AuditService(new InMemoryAuditEventRepository());
        com.restaurant.pos.config.AppConfig config = new com.restaurant.pos.config.AppConfig();
        AuthService authService = new AuthService(userRepository, auditService, config, null);
        categoryService = new CategoryService(categoryRepository, menuItemRepository, authService, auditService);
    }

    @Test
    void deleteSucceedsWhenCategoryHasNoMenuItems() {
        Category category = categoryService.create("Drinks", 1, 1L, "admin");

        categoryService.delete(category.id(), 1L, "admin");

        assertTrue(categoryRepository.findAllOrdered().isEmpty());
    }

    @Test
    void deleteRejectedWhenCategoryStillHasMenuItems() {
        Category category = categoryService.create("Drinks", 1, 1L, "admin");
        menuItemRepository.insert(MenuItem.builder()
                .name("Soda")
                .price(Money.of(new BigDecimal("25.00")))
                .categoryId(category.id())
                .build());

        assertThrows(CategoryInUseException.class, () -> categoryService.delete(category.id(), 1L, "admin"));
    }

    @Test
    void findAllOrderedReturnsCategoriesByDisplayOrder() {
        categoryService.create("Desserts", 3, 1L, "admin");
        categoryService.create("Meals", 1, 1L, "admin");
        categoryService.create("Drinks", 2, 1L, "admin");

        var ordered = categoryService.findAllOrdered();

        assertEquals("Meals", ordered.get(0).name());
        assertEquals("Drinks", ordered.get(1).name());
        assertEquals("Desserts", ordered.get(2).name());
    }
}
