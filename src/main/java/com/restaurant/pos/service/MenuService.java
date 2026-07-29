package com.restaurant.pos.service;

import com.restaurant.pos.model.AuditEventType;
import com.restaurant.pos.model.MenuItem;
import com.restaurant.pos.repository.MenuItemRepository;

import java.util.List;

public final class MenuService {

    private final MenuItemRepository menuItemRepository;
    private final AuthService authService;
    private final AuditService auditService;

    public MenuService(MenuItemRepository menuItemRepository, AuthService authService, AuditService auditService) {
        this.menuItemRepository = menuItemRepository;
        this.authService = authService;
        this.auditService = auditService;
    }

    public List<MenuItem> findAll() {
        return menuItemRepository.findAll();
    }

    public List<MenuItem> findAllAvailable() {
        return menuItemRepository.findAllAvailable();
    }

    public MenuItem create(MenuItem item, long actingUserId, String actingUsername) {
        authService.requireAdmin(actingUserId);
        validateItem(item);
        MenuItem created = menuItemRepository.insert(item);
        auditService.record(AuditEventType.MENU_ITEM_CREATED, actingUserId, actingUsername, created.name());
        return created;
    }

    public void update(MenuItem item, long actingUserId, String actingUsername) {
        authService.requireAdmin(actingUserId);
        validateItem(item);
        menuItemRepository.update(item);
        auditService.record(AuditEventType.MENU_ITEM_UPDATED, actingUserId, actingUsername, item.name());
    }

    private static void validateItem(MenuItem item) {
        if (item == null) {
            throw new IllegalArgumentException("MenuItem must not be null.");
        }
        if (item.name() == null || item.name().isBlank() || item.name().length() > 100) {
            throw new IllegalArgumentException("MenuItem name must be non-empty and at most 100 characters.");
        }
    }

    public void setAvailability(long itemId, boolean available, long actingUserId, String actingUsername) {
        menuItemRepository.setAvailability(itemId, available);
        auditService.record(AuditEventType.MENU_ITEM_AVAILABILITY_CHANGED, actingUserId, actingUsername,
                "id=" + itemId + " available=" + available);
    }
}
