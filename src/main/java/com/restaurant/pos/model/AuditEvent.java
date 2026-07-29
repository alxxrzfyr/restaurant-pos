package com.restaurant.pos.model;

import java.time.Instant;
import java.util.Objects;

public final class AuditEvent {

    private final Long id;
    private final Instant occurredAt;
    private final Long userId;
    private final String username;
    private final AuditEventType eventType;
    private final String details;
    private final String hash;
    private final String prevHash;

    private AuditEvent(Builder builder) {
        this.id = builder.id;
        this.occurredAt = Objects.requireNonNull(builder.occurredAt, "occurredAt is required");
        this.userId = builder.userId;
        this.username = builder.username;
        this.eventType = Objects.requireNonNull(builder.eventType, "eventType is required");
        this.details = builder.details;
        this.hash = builder.hash;
        this.prevHash = builder.prevHash;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long id() {
        return id;
    }

    public Instant occurredAt() {
        return occurredAt;
    }

    public Long userId() {
        return userId;
    }

    public String username() {
        return username;
    }

    public AuditEventType eventType() {
        return eventType;
    }

    public String details() {
        return details;
    }

    public String hash() {
        return hash;
    }

    public String prevHash() {
        return prevHash;
    }

    public static final class Builder {
        private Long id;
        private Instant occurredAt;
        private Long userId;
        private String username;
        private AuditEventType eventType;
        private String details;
        private String hash;
        private String prevHash;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder occurredAt(Instant occurredAt) {
            this.occurredAt = occurredAt;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder eventType(AuditEventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder details(String details) {
            this.details = details;
            return this;
        }

        public Builder hash(String hash) {
            this.hash = hash;
            return this;
        }

        public Builder prevHash(String prevHash) {
            this.prevHash = prevHash;
            return this;
        }

        public AuditEvent build() {
            return new AuditEvent(this);
        }
    }
}
