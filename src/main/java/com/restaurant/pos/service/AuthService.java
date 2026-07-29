package com.restaurant.pos.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.restaurant.pos.config.AppConfig;
import com.restaurant.pos.exception.AccountLockedException;
import com.restaurant.pos.exception.AuthenticationException;
import com.restaurant.pos.model.AuditEventType;
import com.restaurant.pos.model.User;
import com.restaurant.pos.repository.UserRepository;
import com.restaurant.pos.security.LoginRateLimiter;

import java.time.Duration;
import java.time.Instant;
import com.restaurant.pos.model.Role;
import com.restaurant.pos.security.UnauthorizedException;

public final class AuthService {

    private static final int BCRYPT_COST = 12;
    public static final int MAX_USERNAME_LENGTH = 50;
    public static final int MAX_PASSWORD_LENGTH = 128;
    private static final String DUMMY_HASH = BCrypt.withDefaults().hashToString(BCRYPT_COST, "dummy_constant_time_security_hash".toCharArray());

    private final UserRepository userRepository;
    private final AuditService auditService;
    private final LoginRateLimiter rateLimiter;
    private final int maxFailedAttempts;
    private final Duration lockoutWindow;
    private final Duration lockoutDuration;

    public AuthService(UserRepository userRepository, AuditService auditService, AppConfig config, LoginRateLimiter rateLimiter) {
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.rateLimiter = rateLimiter;
        this.maxFailedAttempts = config.loginMaxFailedAttempts();
        this.lockoutWindow = Duration.ofMinutes(config.loginLockoutWindowMinutes());
        this.lockoutDuration = Duration.ofMinutes(config.loginLockoutDurationMinutes());
    }

    public User login(String username, char[] password) {
        if (username == null || username.length() > MAX_USERNAME_LENGTH || password == null || password.length > MAX_PASSWORD_LENGTH) {
            auditService.record(AuditEventType.LOGIN_FAILURE, null, username, "invalid input size or format");
            throw new AuthenticationException("Invalid username or password.");
        }

        if (!rateLimiter.tryAcquire(username)) {
            auditService.record(AuditEventType.LOGIN_FAILURE, null, username, "rate limit exceeded");
            throw new AuthenticationException("Too many login attempts. Please wait a minute before trying again.");
        }

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            verifyPassword(password, DUMMY_HASH);
            auditService.record(AuditEventType.LOGIN_FAILURE, null, username, "unknown username");
            throw new AuthenticationException("Invalid username or password.");
        }

        Instant now = Instant.now();
        if (user.isCurrentlyLocked(now)) {
            auditService.record(AuditEventType.LOGIN_FAILURE, user.id(), username, "account locked");
            throw new AccountLockedException("Account is locked. Try again later.", user.lockedUntil());
        }

        if (!user.active()) {
            auditService.record(AuditEventType.LOGIN_FAILURE, user.id(), username, "account disabled");
            throw new AuthenticationException("This account has been disabled.");
        }

        if (!verifyPassword(password, user.passwordHash())) {
            recordFailedAttempt(user, now);
            throw new AuthenticationException("Invalid username or password.");
        }

        clearFailedAttempts(user);
        rateLimiter.reset(username);
        auditService.record(AuditEventType.LOGIN_SUCCESS, user.id(), username, null);
        return user;
    }

    public void logout(User user) {
        if (user != null) {
            auditService.record(AuditEventType.LOGOUT, user.id(), user.username(), null);
        }
    }

    public String hashPassword(char[] password) {
        if (password == null || password.length > MAX_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must not be null or exceed " + MAX_PASSWORD_LENGTH + " characters.");
        }
        return BCrypt.withDefaults().hashToString(BCRYPT_COST, password);
    }

    private boolean verifyPassword(char[] password, String hash) {
        return BCrypt.verifyer().verify(password, hash).verified;
    }

    private void recordFailedAttempt(User user, Instant now) {
        boolean withinWindow = user.lastFailedLoginAt() != null
                && now.isBefore(user.lastFailedLoginAt().plus(lockoutWindow));
        int attempts = (withinWindow ? user.failedLoginAttempts() : 0) + 1;

        User.Builder updated = user.toBuilder()
                .failedLoginAttempts(attempts)
                .lastFailedLoginAt(now);

        if (attempts >= maxFailedAttempts) {
            updated.lockedUntil(now.plus(lockoutDuration));
            userRepository.update(updated.build());
            auditService.record(AuditEventType.ACCOUNT_LOCKED, user.id(), user.username(),
                    "locked after " + attempts + " failed attempts");
        } else {
            userRepository.update(updated.build());
            auditService.record(AuditEventType.LOGIN_FAILURE, user.id(), user.username(),
                    "attempt " + attempts + " of " + maxFailedAttempts);
        }
    }

    private void clearFailedAttempts(User user) {
        if (user.failedLoginAttempts() > 0 || user.lockedUntil() != null) {
            userRepository.update(user.toBuilder()
                    .failedLoginAttempts(0)
                    .lastFailedLoginAt(null)
                    .lockedUntil(null)
                    .build());
        }
    }

    public void requireAdmin(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found: " + userId));
        requireAdmin(user);
    }

    public void requireAdmin(User user) {
        if (user.role() != Role.ADMINISTRATOR) {
            auditService.record(AuditEventType.SECURITY_VIOLATION, user.id(), user.username(), "Unauthorized admin action attempt");
            throw new UnauthorizedException("User does not have admin privileges.");
        }
    }
}
