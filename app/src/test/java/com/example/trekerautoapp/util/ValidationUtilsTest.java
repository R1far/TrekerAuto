package com.example.trekerautoapp.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.OptionalDouble;
import java.util.OptionalLong;

import org.junit.Test;

public class ValidationUtilsTest {

    @Test
    public void normalizePlate_uppercaseAndTrim() {
        String normalized = ValidationUtils.normalizePlate(" а123аа 77 ");
        assertEquals("А123АА 77", normalized);
    }

    @Test
    public void parseNonNegativeLong_invalidText_returnsEmpty() {
        OptionalLong parsed = ValidationUtils.parseNonNegativeLong("1000р");
        assertFalse(parsed.isPresent());
    }

    @Test
    public void parseNonNegativeLong_negative_returnsEmpty() {
        OptionalLong parsed = ValidationUtils.parseNonNegativeLong("-1");
        assertFalse(parsed.isPresent());
    }

    @Test
    public void parseNonNegativeLong_validNumber_returnsValue() {
        OptionalLong parsed = ValidationUtils.parseNonNegativeLong("89001234567");
        assertTrue(parsed.isPresent());
        assertEquals(89001234567L, parsed.getAsLong());
    }

    @Test
    public void parseNonNegativeCost_withComma_returnsValue() {
        OptionalDouble parsed = ValidationUtils.parseNonNegativeCost("1250,50");
        assertTrue(parsed.isPresent());
        assertEquals(1250.50D, parsed.getAsDouble(), 0.0001D);
    }

    @Test
    public void parseNonNegativeCost_text_returnsEmpty() {
        OptionalDouble parsed = ValidationUtils.parseNonNegativeCost("1000р");
        assertFalse(parsed.isPresent());
    }

    @Test
    public void parseNonNegativeCost_negative_returnsEmpty() {
        OptionalDouble parsed = ValidationUtils.parseNonNegativeCost("-10");
        assertFalse(parsed.isPresent());
    }

    @Test
    public void isValidDate_wrongFormat_returnsFalse() {
        assertFalse(ValidationUtils.isValidDate("2026-01-01"));
    }

    @Test
    public void isValidDate_correctFormat_returnsTrue() {
        assertTrue(ValidationUtils.isValidDate("01.01.2026"));
    }

    @Test
    public void isValidEmail_invalid_returnsFalse() {
        assertFalse(ValidationUtils.isValidEmail("petrexample.com"));
    }

    @Test
    public void isValidEmail_valid_returnsTrue() {
        assertTrue(ValidationUtils.isValidEmail("petr@example.com"));
    }
}
