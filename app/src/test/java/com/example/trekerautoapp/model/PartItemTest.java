package com.example.trekerautoapp.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PartItemTest {

    @Test
    public void hasKnownLastServiceMileage_flagTrue_returnsTrue() {
        PartItem item = new PartItem("Фильтр", "Проверить", 10000, 0, true, 1L);
        assertTrue(item.hasKnownLastServiceMileage());
    }

    @Test
    public void hasKnownLastServiceMileage_mileageGreaterThanZero_returnsTrue() {
        PartItem item = new PartItem("Фильтр", "Проверить", 10000, 15000, false, 1L);
        assertTrue(item.hasKnownLastServiceMileage());
    }

    @Test
    public void hasKnownLastServiceMileage_noFlagAndZeroMileage_returnsFalse() {
        PartItem item = new PartItem("Фильтр", "Проверить", 10000, 0, false, 1L);
        assertFalse(item.hasKnownLastServiceMileage());
    }
}
