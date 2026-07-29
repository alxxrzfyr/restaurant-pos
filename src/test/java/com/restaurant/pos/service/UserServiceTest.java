package com.restaurant.pos.service;

import com.restaurant.pos.config.AppConfig;
import com.restaurant.pos.exception.DuplicateUsernameException;
import com.restaurant.pos.model.Role;
import com.restaurant.pos.model.User;
import com.restaurant.pos.testutil.InMemoryAuditEventRepository;
import com.restaurant.pos.testutil.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserServiceTest {

    private InMemoryUserRepository userRepository;
    private AuthService authService;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        userRepository.insert(User.builder().id(1L).username("admin").displayName("Admin").role(Role.ADMINISTRATOR).passwordHash("hash").active(true).build());
        AuditService auditService = new AuditService(new InMemoryAuditEventRepository());
        com.restaurant.pos.security.LoginRateLimiter rateLimiter = com.restaurant.pos.security.LoginRateLimiter.createDefault();
        authService = new AuthService(userRepository, auditService, new AppConfig(), rateLimiter);
        userService = new UserService(userRepository, authService, auditService);
    }

    @Test
    void createRejectsDuplicateUsername() {
        userService.create("cashier1", "Cashier One", "pass1234".toCharArray(), Role.CASHIER, 1L, "admin");

        assertThrows(DuplicateUsernameException.class, () -> userService.create(
                "cashier1", "Another Name", "pass5678".toCharArray(), Role.CASHIER, 1L, "admin"));
    }

    @Test
    void setActiveDisablesAccountWithoutDeletingIt() {
        User created = userService.create("cashier1", "Cashier One", "pass1234".toCharArray(), Role.CASHIER, 1L, "admin");

        userService.setActive(created.id(), false, 1L, "admin");

        User reloaded = userRepository.findById(created.id()).orElseThrow();
        assertFalse(reloaded.active());
        assertTrue(userRepository.findAll().contains(reloaded));
    }

    @Test
    void resetPasswordAllowsLoginWithNewPasswordOnly() {
        User created = userService.create("cashier1", "Cashier One", "oldPassword".toCharArray(), Role.CASHIER, 1L, "admin");

        userService.resetPassword(created.id(), "newPassword".toCharArray(), 1L, "admin");

        assertThrows(Exception.class, () -> authService.login("cashier1", "oldPassword".toCharArray()));
        User loggedIn = authService.login("cashier1", "newPassword".toCharArray());
        assertEquals("cashier1", loggedIn.username());
    }

    @Test
    void changeRolePromotesUserToAdministrator() {
        User created = userService.create("cashier1", "Cashier One", "pass1234".toCharArray(), Role.CASHIER, 1L, "admin");

        userService.changeRole(created.id(), Role.ADMINISTRATOR, 1L, "admin");

        assertEquals(Role.ADMINISTRATOR, userRepository.findById(created.id()).orElseThrow().role());
    }
}
