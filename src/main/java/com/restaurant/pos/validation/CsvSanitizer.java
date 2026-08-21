package com.restaurant.pos.validation;

public final class CsvSanitizer {
    private CsvSanitizer() {}

    public static String sanitize(Object value) {
        if (value == null) return "";
        String str = value.toString();
        if (str.isEmpty()) return str;

        char firstChar = str.charAt(0);
        if (firstChar == '=' || firstChar == '+' || firstChar == '-' || firstChar == '@' || firstChar == '\t' || firstChar == '\r') {
            return "'" + str;
        }
        return str;
    }

    public static Object[] sanitizeArray(Object... values) {
        Object[] sanitized = new Object[values.length];
        for (int i = 0; i < values.length; i++) {
            sanitized[i] = sanitize(values[i]);
        }
        return sanitized;
    }
}
