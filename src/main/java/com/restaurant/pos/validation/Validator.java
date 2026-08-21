package com.restaurant.pos.validation;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public final class Validator {

    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9 \\-.,&()']+$");
    private static final Pattern SKU_PATTERN = Pattern.compile("^[a-zA-Z0-9\\-]+$");

    private Validator() {}

    public static void requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be empty.");
        }
    }

    public static void requireValidName(String value, String fieldName) {
        requireNonEmpty(value, fieldName);
        if (value.length() > 100) {
            throw new ValidationException(fieldName + " is too long (max 100 chars).");
        }
        if (!NAME_PATTERN.matcher(value).matches()) {
            throw new ValidationException(fieldName + " contains invalid characters.");
        }
    }

    public static void requireValidSku(String value, String fieldName) {
        if (value != null && !value.trim().isEmpty()) {
            if (value.length() > 50) {
                throw new ValidationException(fieldName + " is too long (max 50 chars).");
            }
            if (!SKU_PATTERN.matcher(value).matches()) {
                throw new ValidationException(fieldName + " must only contain alphanumeric characters and dashes.");
            }
        }
    }

    public static void requireNonNegative(BigDecimal value, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + " cannot be null.");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException(fieldName + " cannot be negative.");
        }
    }

    public static void requireNonNegative(long value, String fieldName) {
        if (value < 0) {
            throw new ValidationException(fieldName + " cannot be negative.");
        }
    }

    public static void requireNonNegative(int value, String fieldName) {
        if (value < 0) {
            throw new ValidationException(fieldName + " cannot be negative.");
        }
    }

    public static void requireDiscountBounds(BigDecimal percentage, String fieldName) {
        requireNonNegative(percentage, fieldName);
        if (percentage.compareTo(new BigDecimal("100")) > 0) {
            throw new ValidationException(fieldName + " cannot exceed 100%.");
        }
    }
}
