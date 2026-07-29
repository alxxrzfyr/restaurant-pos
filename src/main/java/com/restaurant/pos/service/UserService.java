package com.restaurant.pos.service;

import com.restaurant.pos.exception.DuplicateUsernameException;
import com.restaurant.pos.model.AuditEventType;
import com.restaurant.pos.model.Role;
import com.restaurant.pos.model.User;
import com.restaurant.pos.repository.UserRepository;

import java.util.List;

public final class UserService {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final AuditService auditService;

    public UserService(UserRepository userRepository, AuthService authService, AuditService auditService) {
        this.userRepository = userRepository;
        this.authService = authService;
        this.auditService = auditService;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User create(String username, String displayName, char[] password, Role role,
                        long actingUserId, String actingUsername) {
        return create(username, displayName, password, role, null, actingUserId, actingUsername);
    }

    private static final java.util.regex.Pattern USERNAME_PATTERN = java.util.regex.Pattern.compile("^[a-zA-Z0-9_.-]{3,50}$");

    public User create(String username, String displayName, char[] password, Role role, String photoPath,
                        long actingUserId, String actingUsername) {
        authService.requireAdmin(actingUserId);
        validateUsername(username);
        validatePassword(password);
        if (displayName == null || displayName.isBlank() || displayName.length() > 100) {
            throw new IllegalArgumentException("Display name must be non-empty and at most 100 characters.");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            throw new DuplicateUsernameException(username);
        }
        User user = User.builder()
                .username(username)
                .displayName(displayName)
                .passwordHash(authService.hashPassword(password))
                .role(role)
                .photoPath(photoPath)
                .build();
        User created = userRepository.insert(user);
        auditService.record(AuditEventType.USER_CREATED, actingUserId, actingUsername, created.username());
        return created;
    }

    public void setActive(long userId, boolean active, long actingUserId, String actingUsername) {
        authService.requireAdmin(actingUserId);
        User user = requireById(userId);
        userRepository.update(user.toBuilder().active(active).build());
        auditService.record(active ? AuditEventType.USER_ENABLED : AuditEventType.USER_DISABLED,
                actingUserId, actingUsername, user.username());
    }

    public void resetPassword(long userId, char[] newPassword, long actingUserId, String actingUsername) {
        authService.requireAdmin(actingUserId);
        validatePassword(newPassword);
        User user = requireById(userId);
        userRepository.update(user.toBuilder()
                .passwordHash(authService.hashPassword(newPassword))
                .failedLoginAttempts(0)
                .lastFailedLoginAt(null)
                .lockedUntil(null)
                .build());
        auditService.record(AuditEventType.USER_PASSWORD_RESET, actingUserId, actingUsername, user.username());
    }

    public void clearRequiresPasswordChange(long userId) {
        User user = requireById(userId);
        userRepository.update(user.toBuilder().requiresPasswordChange(false).build());
    }

    public void changeRole(long userId, Role newRole, long actingUserId, String actingUsername) {
        authService.requireAdmin(actingUserId);
        User user = requireById(userId);
        userRepository.update(user.toBuilder().role(newRole).build());
        auditService.record(AuditEventType.USER_UPDATED, actingUserId, actingUsername,
                user.username() + " role -> " + newRole);
    }

    public void updateUser(long userId, String displayName, String photoPath, long actingUserId, String actingUsername) {
        authService.requireAdmin(actingUserId);
        if (displayName == null || displayName.isBlank() || displayName.length() > 100) {
            throw new IllegalArgumentException("Display name must be non-empty and at most 100 characters.");
        }
        User user = requireById(userId);
        userRepository.update(user.toBuilder()
                .displayName(displayName)
                .photoPath(photoPath)
                .build());
        auditService.record(AuditEventType.USER_UPDATED, actingUserId, actingUsername,
                user.username() + " profile info updated");
    }

    public void updatePhotoPath(long userId, String photoPath) {
        User user = requireById(userId);
        userRepository.update(user.toBuilder().photoPath(photoPath).build());
    }

    private static void validateUsername(String username) {
        if (username == null || !USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException("Username must be 3-50 alphanumeric characters (including _, -, .).");
        }
    }

    private static void validatePassword(char[] password) {
        if (password == null || password.length < 6 || password.length > 128) {
            throw new IllegalArgumentException("Password must be between 6 and 128 characters.");
        }
    }

    private User requireById(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("No user with id " + userId));
    }
}
