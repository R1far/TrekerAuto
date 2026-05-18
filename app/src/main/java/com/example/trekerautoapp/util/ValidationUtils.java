package com.example.trekerautoapp.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.regex.Pattern;

public final class ValidationUtils {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );

    private ValidationUtils() {
    }

    public static String normalizePlate(String rawPlate) {
        if (rawPlate == null) {
            return "";
        }
        return rawPlate.trim().toUpperCase(Locale.ROOT);
    }

    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static OptionalLong parseNonNegativeLong(String rawNumber) {
        if (rawNumber == null || rawNumber.trim().isEmpty()) {
            return OptionalLong.empty();
        }
        try {
            long value = Long.parseLong(rawNumber.trim());
            if (value < 0L) {
                return OptionalLong.empty();
            }
            return OptionalLong.of(value);
        } catch (NumberFormatException exception) {
            return OptionalLong.empty();
        }
    }

    public static OptionalDouble parseNonNegativeCost(String rawCost) {
        if (rawCost == null || rawCost.trim().isEmpty()) {
            return OptionalDouble.of(0D);
        }
        try {
            double value = Double.parseDouble(rawCost.trim().replace(',', '.'));
            if (value < 0D) {
                return OptionalDouble.empty();
            }
            return OptionalDouble.of(value);
        } catch (NumberFormatException exception) {
            return OptionalDouble.empty();
        }
    }

    public static boolean isValidDate(String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty()) {
            return false;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        dateFormat.setLenient(false);
        try {
            return dateFormat.parse(rawDate.trim()) != null;
        } catch (ParseException exception) {
            return false;
        }
    }
}
