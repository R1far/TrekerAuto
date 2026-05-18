package com.example.trekerautoapp.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PartIntervalDefaultsTest {

    @Test
    public void defaultAverageIntervalKm_returnsPositiveAverage() {
        long value = PartIntervalDefaults.defaultAverageIntervalKm();
        assertTrue(value > 0L);
        assertEquals(40833L, value);
    }
}
