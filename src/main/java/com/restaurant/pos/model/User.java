package com.restaurant.pos.model;

import java.time.Instant;
import java.util.Objects;

public final class User {

    private final Long id;
    private final String username;
    private final String displayName;
    private final String passwordHash;
    private final Role role;
    private final boolean active;
    private final int failedLoginAttempts;
    private final Instant lastFailedLoginAt;
    private final Instant lockedUntil;
    private final boolean requiresPasswordChange;

    private final String photoPath;

    private User(Builder builder) {
        this.id = builder.id;
        this.username = Objects.requireNonNull(builder.username, "username is required");
        this.displayName = Objects.requireNonNull(builder.displayName, "displayName is required");
        this.passwordHash = Objects.requireNonNull(builder.passwordHash, "passwordHash is required");
        this.role = Objects.requireNonNull(builder.role, "role is required");
        this.active = builder.active;
        this.failedLoginAttempts = builder.failedLoginAttempts;
        this.lastFailedLoginAt = builder.lastFailedLoginAt;
        this.lockedUntil = builder.lockedUntil;
        this.requiresPasswordChange = builder.requiresPasswordChange;
        this.photoPath = builder.photoPath;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder()
                .id(id)
                .username(username)
                .displayName(displayName)
                .passwordHash(passwordHash)
                .role(role)
                .active(active)
                .failedLoginAttempts(failedLoginAttempts)
                .lastFailedLoginAt(lastFailedLoginAt)
                .lockedUntil(lockedUntil)
                .requiresPasswordChange(requiresPasswordChange)
                .photoPath(photoPath);
    }

    public boolean isCurrentlyLocked(Instant now) {
        return lockedUntil != null && now.isBefore(lockedUntil);
    }

    public Long id() {
        return id;
    }

    public String username() {
        return username;
    }

    public String displayName() {
        return displayName;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public Role role() {
        return role;
    }

    public boolean active() {
        return active;
    }

    public int failedLoginAttempts() {
        return failedLoginAttempts;
    }

    public Instant lastFailedLoginAt() {
        return lastFailedLoginAt;
    }

    public Instant lockedUntil() {
        return lockedUntil;
    }

    public boolean requiresPasswordChange() {
        return requiresPasswordChange;
    }

    public String photoPath() {
        return photoPath;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof User other)) {
            return false;
        }
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return username;
    }

    public static final class Builder {
        private Long id;
        private String username;
        private String displayName;
        private String passwordHash;
        private Role role;
        private boolean active = true;
        private int failedLoginAttempts = 0;
        private Instant lastFailedLoginAt = null;
        private Instant lockedUntil = null;
        private boolean requiresPasswordChange = false;
        private String photoPath = null;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder passwordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public Builder role(Role role) {
            this.role = role;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public Builder failedLoginAttempts(int failedLoginAttempts) {
            this.failedLoginAttempts = failedLoginAttempts;
            return this;
        }

        public Builder lastFailedLoginAt(Instant lastFailedLoginAt) {
            this.lastFailedLoginAt = lastFailedLoginAt;
            return this;
        }

        public Builder lockedUntil(Instant lockedUntil) {
            this.lockedUntil = lockedUntil;
            return this;
        }

        public Builder requiresPasswordChange(boolean requiresPasswordChange) {
            this.requiresPasswordChange = requiresPasswordChange;
            return this;
        }

        public Builder photoPath(String photoPath) {
            this.photoPath = photoPath;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}
