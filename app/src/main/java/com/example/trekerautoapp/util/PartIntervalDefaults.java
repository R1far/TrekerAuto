package com.example.trekerautoapp.util;

public final class PartIntervalDefaults {
    private static final long[] TYPICAL_INTERVALS_KM = new long[]{
            10000L, // engine oil
            10000L, // oil filter
            15000L, // air filter
            15000L, // cabin filter
            30000L, // spark plugs
            30000L, // fuel filter
            40000L, // brake fluid
            50000L, // brake pads
            60000L, // transmission oil
            60000L, // coolant
            80000L, // timing belt
            90000L  // drive belt
    };

    private PartIntervalDefaults() {
    }

    public static long defaultAverageIntervalKm() {
        if (TYPICAL_INTERVALS_KM.length == 0) {
            return 15000L;
        }
        long sum = 0L;
        for (long interval : TYPICAL_INTERVALS_KM) {
            sum += Math.max(1L, interval);
        }
        return Math.max(1L, Math.round((double) sum / (double) TYPICAL_INTERVALS_KM.length));
    }
}
