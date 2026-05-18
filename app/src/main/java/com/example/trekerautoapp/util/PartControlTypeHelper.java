package com.example.trekerautoapp.util;

import android.content.Context;

import com.example.trekerautoapp.R;

import java.util.Locale;

public final class PartControlTypeHelper {
    private static final String FALLBACK_REPLACE = "Поменять";
    private static final String FALLBACK_CHECK = "Проверить";

    private PartControlTypeHelper() {
    }

    public static String[] options(Context context) {
        String[] values = context.getResources().getStringArray(R.array.part_control_types);
        if (values.length >= 2) {
            return values;
        }
        return new String[]{FALLBACK_CHECK, FALLBACK_REPLACE};
    }

    public static String defaultValue(Context context) {
        return checkValue(context);
    }

    public static String normalize(Context context, String rawValue) {
        if (rawValue == null) {
            return defaultValue(context);
        }

        String trimmed = rawValue.trim();
        if (trimmed.isEmpty()) {
            return defaultValue(context);
        }

        String replace = replaceValue(context);
        String check = checkValue(context);

        if (trimmed.equalsIgnoreCase(replace)) {
            return replace;
        }
        if (trimmed.equalsIgnoreCase(check)) {
            return check;
        }

        String lower = trimmed.toLowerCase(Locale.ROOT);
        if (lower.contains("замен") || lower.contains("помен") || lower.contains("replace") || lower.contains("change")) {
            return replace;
        }
        if (lower.contains("пров") || lower.contains("check") || lower.contains("inspect")) {
            return check;
        }
        return defaultValue(context);
    }

    public static String replaceValue(Context context) {
        String[] values = options(context);
        for (String value : values) {
            if (value == null) {
                continue;
            }
            String lower = value.toLowerCase(Locale.ROOT);
            if (lower.contains("замен") || lower.contains("помен") || lower.contains("replace") || lower.contains("change")) {
                return value;
            }
        }
        return values[values.length - 1];
    }

    public static String checkValue(Context context) {
        String[] values = options(context);
        for (String value : values) {
            if (value == null) {
                continue;
            }
            String lower = value.toLowerCase(Locale.ROOT);
            if (lower.contains("пров") || lower.contains("check") || lower.contains("inspect")) {
                return value;
            }
        }
        return values[0];
    }
}
