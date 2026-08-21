package com.restaurant.pos.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class AppConfig {

    private static final String RESOURCE_NAME = "application.properties";

    private final Properties properties;

    public AppConfig() {
        this.properties = loadProperties();
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream(RESOURCE_NAME)) {
            if (in == null) {
                throw new IllegalStateException("Missing required resource: " + RESOURCE_NAME);
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + RESOURCE_NAME, e);
        }
        return props;
    }

    public String dbFile() {
        return resolveString("db.file", "DB_FILE", "restaurant-pos.db");
    }

    public int dbPoolMaxSize() {
        return getInt("db.pool.maxSize", "DB_POOL_MAX_SIZE", 4);
    }

    public int dbPoolMinIdle() {
        return getInt("db.pool.minIdle", "DB_POOL_MIN_IDLE", 1);
    }

    public long dbConnectionTimeoutMs() {
        return getLong("db.pool.connectionTimeoutMs", "DB_POOL_CONNECTION_TIMEOUT_MS", 10_000);
    }

    public int dbBusyTimeoutMs() {
        return getInt("db.busyTimeoutMs", "DB_BUSY_TIMEOUT_MS", 30_000);
    }

    public int loginMaxFailedAttempts() {
        return getInt("security.login.maxFailedAttempts", "SECURITY_LOGIN_MAX_FAILED_ATTEMPTS", 5);
    }

    public int loginLockoutWindowMinutes() {
        return getInt("security.login.lockoutWindowMinutes", "SECURITY_LOGIN_LOCKOUT_WINDOW_MINUTES", 15);
    }

    public int loginLockoutDurationMinutes() {
        return getInt("security.login.lockoutDurationMinutes", "SECURITY_LOGIN_LOCKOUT_DURATION_MINUTES", 15);
    }

    public String appName() {
        return resolveString("app.name", "APP_NAME", "Restaurant POS");
    }

    public String appVersion() {
        return resolveString("app.version", "APP_VERSION", "1.0.0");
    }

    public String appTimezone() {
        return resolveString("app.timezone", "APP_TIMEZONE", "Asia/Manila");
    }

    private String resolveString(String key, String envKey, String defaultValue) {
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.isBlank()) {
            return sysProp;
        }
        String envVar = System.getenv(envKey);
        if (envVar != null && !envVar.isBlank()) {
            return envVar;
        }
        return properties.getProperty(key, defaultValue);
    }

    private int getInt(String key, String envKey, int defaultValue) {
        String value = resolveString(key, envKey, null);
        return value == null ? defaultValue : Integer.parseInt(value.trim());
    }

    private long getLong(String key, String envKey, long defaultValue) {
        String value = resolveString(key, envKey, null);
        return value == null ? defaultValue : Long.parseLong(value.trim());
    }
}
