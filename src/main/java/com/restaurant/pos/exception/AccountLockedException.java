package com.restaurant.pos.exception;

import java.time.Instant;

public class AccountLockedException extends AuthenticationException {

    private final Instant lockedUntil;

    public AccountLockedException(String message, Instant lockedUntil) {
        super(message);
        this.lockedUntil = lockedUntil;
    }

    public Instant lockedUntil() {
        return lockedUntil;
    }
}
