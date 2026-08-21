package com.restaurant.pos.security;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginRateLimiterTest {

    @Test
    void allowsAttemptsUpToLimitAndBlocksExcess() {
        LoginRateLimiter rateLimiter = new LoginRateLimiter(3, Duration.ofMinutes(1));

        assertTrue(rateLimiter.tryAcquire("user1"));
        assertTrue(rateLimiter.tryAcquire("user1"));
        assertTrue(rateLimiter.tryAcquire("user1"));

        assertFalse(rateLimiter.tryAcquire("user1"));
    }

    @Test
    void separateKeysHaveIndependentLimits() {
        LoginRateLimiter rateLimiter = new LoginRateLimiter(2, Duration.ofMinutes(1));

        assertTrue(rateLimiter.tryAcquire("userA"));
        assertTrue(rateLimiter.tryAcquire("userA"));
        assertFalse(rateLimiter.tryAcquire("userA"));

        assertTrue(rateLimiter.tryAcquire("userB"));
    }

    @Test
    void resetClearsHistoryForGivenKey() {
        LoginRateLimiter rateLimiter = new LoginRateLimiter(1, Duration.ofMinutes(1));

        assertTrue(rateLimiter.tryAcquire("admin"));
        assertFalse(rateLimiter.tryAcquire("admin"));

        rateLimiter.reset("admin");

        assertTrue(rateLimiter.tryAcquire("admin"));
    }
}
