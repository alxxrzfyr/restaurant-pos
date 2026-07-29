package com.restaurant.pos.service;

import com.restaurant.pos.config.AppConfig;
import com.restaurant.pos.exception.AccountLockedException;
import com.restaurant.pos.exception.AuthenticationException;
import com.restaurant.pos.model.Role;
import com.restaurant.pos.model.User;
import com.restaurant.pos.testutil.InMemoryAuditEventRepository;
import com.restaurant.pos.testutil.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServiceTest {

    private InMemoryUserRepository userRepository;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        AuditService auditService = new AuditService(new InMemoryAuditEventRepository());
        com.restaurant.pos.security.LoginRateLimiter rateLimiter = com.restaurant.pos.security.LoginRateLimiter.createDefault();
        authService = new AuthService(userRepository, auditService, new AppConfig(), rateLimiter);
    }

    private User createUser(String username, char[] password) {
        User user = User.builder()
                .username(username)
                .displayName("Test User")
                .passwordHash(authService.hashPassword(password))
                .role(Role.CASHIER)
                .build();
        return userRepository.insert(user);
    }

    @Test
    void hashPasswordProducesADifferentHashEachTime() {
        String hash1 = authService.hashPassword("secret123".toCharArray());
        String hash2 = authService.hashPassword("secret123".toCharArray());
        assertNotEquals(hash1, hash2);
    }

    @Test
    void loginSucceedsWithCorrectPassword() {
        createUser("cashier1", "correctPassword".toCharArray());
        User loggedIn = authService.login("cashier1", "correctPassword".toCharArray());
        assertEquals("cashier1", loggedIn.username());
    }

    @Test
    void loginFailsWithUnknownUsername() {
        assertThrows(AuthenticationException.class, () -> authService.login("nobody", "anything".toCharArray()));
    }

    @Test
    void loginFailsWithWrongPassword() {
        createUser("cashier1", "correctPassword".toCharArray());
        assertThrows(AuthenticationException.class, () -> authService.login("cashier1", "wrongPassword".toCharArray()));
    }

    @Test
    void loginFailsForDisabledAccount() {
        User user = createUser("cashier1", "correctPassword".toCharArray());
        userRepository.update(user.toBuilder().active(false).build());

        assertThrows(AuthenticationException.class, () -> authService.login("cashier1", "correctPassword".toCharArray()));
    }

    @Test
    void accountLocksAfterConfiguredNumberOfFailedAttempts() {
        createUser("cashier1", "correctPassword".toCharArray());
        int maxAttempts = new AppConfig().loginMaxFailedAttempts();

        for (int i = 0; i < maxAttempts; i++) {
            assertThrows(AuthenticationException.class,
                    () -> authService.login("cashier1", "wrongPassword".toCharArray()));
        }

        AccountLockedException locked = assertThrows(AccountLockedException.class,
                () -> authService.login("cashier1", "correctPassword".toCharArray()));
        assertTrue(locked.lockedUntil().isAfter(java.time.Instant.now()));
    }

    @Test
    void successfulLoginClearsPriorFailedAttempts() {
        createUser("cashier1", "correctPassword".toCharArray());
        assertThrows(AuthenticationException.class, () -> authService.login("cashier1", "wrongPassword".toCharArray()));

        authService.login("cashier1", "correctPassword".toCharArray());

        User reloaded = userRepository.findByUsername("cashier1").orElseThrow();
        assertEquals(0, reloaded.failedLoginAttempts());
    }

    @Test
    void loginRejectsExcessiveAttemptsWhenRateLimited() {
        createUser("spammer", "password123".toCharArray());
        com.restaurant.pos.security.LoginRateLimiter strictLimiter =
                new com.restaurant.pos.security.LoginRateLimiter(2, java.time.Duration.ofMinutes(1));
        AuthService throttledAuthService = new AuthService(
                userRepository,
                new AuditService(new InMemoryAuditEventRepository()),
                new AppConfig(),
                strictLimiter
        );

        assertThrows(AuthenticationException.class, () -> throttledAuthService.login("spammer", "wrong".toCharArray()));
        assertThrows(AuthenticationException.class, () -> throttledAuthService.login("spammer", "wrong".toCharArray()));

        // 3rd attempt triggered by rate limiter rejection
        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> throttledAuthService.login("spammer", "password123".toCharArray()));
        assertTrue(ex.getMessage().contains("Too many login attempts"));
    }

    @Test
    void loginRejectsOverlyLongUsernameOrPassword() {
        String longUsername = "a".repeat(100);
        char[] longPassword = "p".repeat(200).toCharArray();

        assertThrows(AuthenticationException.class, () -> authService.login(longUsername, "password".toCharArray()));
        assertThrows(AuthenticationException.class, () -> authService.login("cashier1", longPassword));
    }
}
