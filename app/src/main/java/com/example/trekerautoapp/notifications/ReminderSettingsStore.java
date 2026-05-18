package com.example.trekerautoapp.notifications;

import android.content.Context;
import android.content.SharedPreferences;

public class ReminderSettingsStore {
    public static final int MIN_REMINDER_DAYS = 1;
    public static final int MAX_REMINDER_DAYS = 30;

    private static final int DEFAULT_REMINDER_DAYS = 3;
    private static final boolean DEFAULT_NO_PARTS_ENABLED = true;
    private static final int DEFAULT_NO_PARTS_DAYS = 7;

    private static final String PREFS_NAME = "part_reminder_settings";
    private static final String KEY_REMINDER_INTERVAL_DAYS = "reminder_interval_days";
    private static final String KEY_NO_PARTS_ENABLED = "no_parts_reminder_enabled";
    private static final String KEY_NO_PARTS_INTERVAL_DAYS = "no_parts_reminder_interval_days";

    private static final String KEY_LAST_SENT_PREFIX = "last_sent_";
    private static final String KEY_LAST_NO_PARTS_PREFIX = "last_no_parts_sent_";

    private final SharedPreferences preferences;

    public ReminderSettingsStore(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public int getReminderIntervalDays() {
        int stored = preferences.getInt(KEY_REMINDER_INTERVAL_DAYS, DEFAULT_REMINDER_DAYS);
        return clampDays(stored);
    }

    public void setReminderIntervalDays(int days) {
        preferences.edit()
                .putInt(KEY_REMINDER_INTERVAL_DAYS, clampDays(days))
                .apply();
    }

    public boolean isNoPartsReminderEnabled() {
        return preferences.getBoolean(KEY_NO_PARTS_ENABLED, DEFAULT_NO_PARTS_ENABLED);
    }

    public void setNoPartsReminderEnabled(boolean enabled) {
        preferences.edit()
                .putBoolean(KEY_NO_PARTS_ENABLED, enabled)
                .apply();
    }

    public int getNoPartsReminderIntervalDays() {
        int stored = preferences.getInt(KEY_NO_PARTS_INTERVAL_DAYS, DEFAULT_NO_PARTS_DAYS);
        return clampDays(stored);
    }

    public void setNoPartsReminderIntervalDays(int days) {
        preferences.edit()
                .putInt(KEY_NO_PARTS_INTERVAL_DAYS, clampDays(days))
                .apply();
    }

    public long getLastNotificationAt(String ownerId, String carId, String partId, String type) {
        return preferences.getLong(notificationKey(ownerId, carId, partId, type), 0L);
    }

    public void setLastNotificationAt(String ownerId, String carId, String partId, String type, long timestamp) {
        preferences.edit()
                .putLong(notificationKey(ownerId, carId, partId, type), timestamp)
                .apply();
    }

    public long getLastNoPartsNotificationAt(String ownerId, String carId) {
        return preferences.getLong(noPartsKey(ownerId, carId), 0L);
    }

    public void setLastNoPartsNotificationAt(String ownerId, String carId, long timestamp) {
        preferences.edit()
                .putLong(noPartsKey(ownerId, carId), timestamp)
                .apply();
    }

    public void clearNoPartsNotification(String ownerId, String carId) {
        preferences.edit()
                .remove(noPartsKey(ownerId, carId))
                .apply();
    }

    public void clearPartState(String ownerId, String carId, String partId) {
        preferences.edit()
                .remove(notificationKey(ownerId, carId, partId, PartReminderWorker.TYPE_WARNING))
                .remove(notificationKey(ownerId, carId, partId, PartReminderWorker.TYPE_URGENT))
                .apply();
    }

    public void clearNotificationType(String ownerId, String carId, String partId, String type) {
        preferences.edit()
                .remove(notificationKey(ownerId, carId, partId, type))
                .apply();
    }

    private String notificationKey(String ownerId, String carId, String partId, String type) {
        return KEY_LAST_SENT_PREFIX + safe(ownerId) + "_" + safe(carId) + "_" + safe(partId) + "_" + safe(type);
    }

    private String noPartsKey(String ownerId, String carId) {
        return KEY_LAST_NO_PARTS_PREFIX + safe(ownerId) + "_" + safe(carId);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private int clampDays(int days) {
        if (days < MIN_REMINDER_DAYS) {
            return MIN_REMINDER_DAYS;
        }
        if (days > MAX_REMINDER_DAYS) {
            return MAX_REMINDER_DAYS;
        }
        return days;
    }
}
