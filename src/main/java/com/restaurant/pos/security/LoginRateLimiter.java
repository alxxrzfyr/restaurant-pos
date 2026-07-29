package com.restaurant.pos.security;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LoginRateLimiter {

    private final int maxAttemptsPerWindow;
    private final Duration windowDuration;
    private final Map<String, Deque<Instant>> attemptHistory = new ConcurrentHashMap<>();

    public LoginRateLimiter(int maxAttemptsPerWindow, Duration windowDuration) {
        if (maxAttemptsPerWindow <= 0) {
            throw new IllegalArgumentException("maxAttemptsPerWindow must be greater than zero");
        }
        if (windowDuration == null || windowDuration.isNegative() || windowDuration.isZero()) {
            throw new IllegalArgumentException("windowDuration must be positive");
        }
        this.maxAttemptsPerWindow = maxAttemptsPerWindow;
        this.windowDuration = windowDuration;
    }

    public static LoginRateLimiter createDefault() {
        return new LoginRateLimiter(10, Duration.ofMinutes(1));
    }

    public boolean tryAcquire(String key) {
        if (key == null || key.isBlank()) {
            key = "unknown";
        }
        Instant now = Instant.now();
        Instant cutoff = now.minus(windowDuration);

        Deque<Instant> timestamps = attemptHistory.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(cutoff)) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= maxAttemptsPerWindow) {
                return false;
            }
            timestamps.addLast(now);
            return true;
        }
    }

    public void reset(String key) {
        if (key != null) {
            attemptHistory.remove(key);
        }
    }
}
