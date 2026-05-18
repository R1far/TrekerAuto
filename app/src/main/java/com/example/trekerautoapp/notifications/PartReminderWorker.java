package com.example.trekerautoapp.notifications;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.trekerautoapp.data.UserIdentityProvider;
import com.example.trekerautoapp.model.Car;
import com.example.trekerautoapp.model.PartItem;
import com.example.trekerautoapp.util.PartControlTypeHelper;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.TimeUnit;

public class PartReminderWorker extends Worker {
    public static final String TYPE_WARNING = "warning";
    public static final String TYPE_URGENT = "urgent";

    private static final int WARNING_WEAR_THRESHOLD = 75;
    private static final long MILLIS_IN_DAY = TimeUnit.DAYS.toMillis(1);

    private final FirebaseFirestore firestore;
    private final ReminderSettingsStore settingsStore;

    public PartReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        firestore = FirebaseFirestore.getInstance();
        settingsStore = new ReminderSettingsStore(context.getApplicationContext());
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Context context = getApplicationContext();
            String ownerId = UserIdentityProvider.resolveOwnerId(context);
            if (ownerId == null || ownerId.trim().isEmpty()) {
                return Result.success();
            }

            PartReminderNotifier.ensureChannel(context);
            int reminderIntervalDays = settingsStore.getReminderIntervalDays();
            boolean noPartsReminderEnabled = settingsStore.isNoPartsReminderEnabled();
            int noPartsReminderIntervalDays = settingsStore.getNoPartsReminderIntervalDays();
            long now = System.currentTimeMillis();

            QuerySnapshot carSnapshot = Tasks.await(
                    firestore.collection("users")
                            .document(ownerId)
                            .collection("cars")
                            .get()
            );

            for (DocumentSnapshot carDocument : carSnapshot.getDocuments()) {
                Car car = carDocument.toObject(Car.class);
                if (car == null) {
                    continue;
                }
                String carId = carDocument.getId();
                long carMileage = car.getMileage();
                String carTitle = buildCarTitle(car);

                QuerySnapshot partSnapshot = Tasks.await(
                        carDocument.getReference()
                                .collection("parts")
                                .get()
                );

                if (partSnapshot.isEmpty()) {
                    if (noPartsReminderEnabled) {
                        handleNoPartsReminder(ownerId, carId, carTitle, noPartsReminderIntervalDays, now);
                    } else {
                        settingsStore.clearNoPartsNotification(ownerId, carId);
                    }
                    continue;
                }

                settingsStore.clearNoPartsNotification(ownerId, carId);
                for (DocumentSnapshot partDocument : partSnapshot.getDocuments()) {
                    PartItem partItem = partDocument.toObject(PartItem.class);
                    if (partItem == null) {
                        continue;
                    }
                    String partId = partDocument.getId();
                    handlePart(ownerId, carId, carTitle, partId, partItem, carMileage, reminderIntervalDays, now);
                }
            }

            return Result.success();
        } catch (Exception exception) {
            return Result.retry();
        }
    }

    private void handlePart(
            String ownerId,
            String carId,
            String carTitle,
            String partId,
            PartItem partItem,
            long carMileage,
            int reminderIntervalDays,
            long now
    ) {
        if (!partItem.hasKnownLastServiceMileage()) {
            settingsStore.clearPartState(ownerId, carId, partId);
            return;
        }

        long intervalKm = Math.max(1L, partItem.getIntervalKm());
        long usedKm = Math.max(0L, carMileage - partItem.getLastServiceMileage());
        long remainingKm = intervalKm - usedKm;
        int wearPercent = (int) Math.min(100L, (usedKm * 100L) / intervalKm);

        if (remainingKm <= 0L) {
            settingsStore.clearNotificationType(ownerId, carId, partId, TYPE_WARNING);
            if (shouldNotify(ownerId, carId, partId, TYPE_URGENT, 1, now)) {
                long overdue = Math.abs(remainingKm);
                String title = buildUrgentTitle(partItem);
                String text = carTitle + ": интервал превышен на " + overdue + " км.";
                int notificationId = notificationId(ownerId, carId, partId, TYPE_URGENT);
                boolean sent = PartReminderNotifier.sendNotification(getApplicationContext(), notificationId, title, text);
                if (sent) {
                    settingsStore.setLastNotificationAt(ownerId, carId, partId, TYPE_URGENT, now);
                }
            }
            return;
        }

        settingsStore.clearNotificationType(ownerId, carId, partId, TYPE_URGENT);
        if (wearPercent >= WARNING_WEAR_THRESHOLD) {
            if (shouldNotify(ownerId, carId, partId, TYPE_WARNING, reminderIntervalDays, now)) {
                String title = "Напоминание: " + safePartName(partItem);
                String text = carTitle + ": осталось около " + remainingKm + " км до обслуживания.";
                int notificationId = notificationId(ownerId, carId, partId, TYPE_WARNING);
                boolean sent = PartReminderNotifier.sendNotification(getApplicationContext(), notificationId, title, text);
                if (sent) {
                    settingsStore.setLastNotificationAt(ownerId, carId, partId, TYPE_WARNING, now);
                }
            }
            return;
        }

        settingsStore.clearPartState(ownerId, carId, partId);
    }

    private void handleNoPartsReminder(
            String ownerId,
            String carId,
            String carTitle,
            int minDaysBetween,
            long now
    ) {
        if (!shouldNotifyNoParts(ownerId, carId, minDaysBetween, now)) {
            return;
        }

        String title = "Добавьте расходники и запчасти";
        String text = carTitle + ": пока нет карточек для контроля обслуживания.";
        int notificationId = notificationId(ownerId, carId, "no_parts", "no_parts");
        boolean sent = PartReminderNotifier.sendNotification(getApplicationContext(), notificationId, title, text);
        if (sent) {
            settingsStore.setLastNoPartsNotificationAt(ownerId, carId, now);
        }
    }

    private boolean shouldNotify(
            String ownerId,
            String carId,
            String partId,
            String type,
            int minDaysBetween,
            long now
    ) {
        long lastNotifiedAt = settingsStore.getLastNotificationAt(ownerId, carId, partId, type);
        if (lastNotifiedAt <= 0L) {
            return true;
        }
        long days = (now - lastNotifiedAt) / MILLIS_IN_DAY;
        return days >= Math.max(1, minDaysBetween);
    }

    private boolean shouldNotifyNoParts(String ownerId, String carId, int minDaysBetween, long now) {
        long lastNotifiedAt = settingsStore.getLastNoPartsNotificationAt(ownerId, carId);
        if (lastNotifiedAt <= 0L) {
            return true;
        }
        long days = (now - lastNotifiedAt) / MILLIS_IN_DAY;
        return days >= Math.max(1, minDaysBetween);
    }

    private String buildCarTitle(Car car) {
        String brand = car.getBrand() == null ? "" : car.getBrand().trim();
        String model = car.getModel() == null ? "" : car.getModel().trim();
        String title = (brand + " " + model).trim();
        return title.isEmpty() ? "Автомобиль" : title;
    }

    private String safePartName(PartItem partItem) {
        String name = partItem.getName();
        if (name == null || name.trim().isEmpty()) {
            return "расходник";
        }
        return name.trim();
    }

    private String buildUrgentTitle(PartItem partItem) {
        String replaceType = PartControlTypeHelper.replaceValue(getApplicationContext());
        String normalizedType = PartControlTypeHelper.normalize(getApplicationContext(), partItem.getControlType());
        if (replaceType.equalsIgnoreCase(normalizedType)) {
            return "Нужно заменить: " + safePartName(partItem);
        }
        return "Нужно проверить: " + safePartName(partItem);
    }

    private int notificationId(String ownerId, String carId, String partId, String type) {
        String key = ownerId + "_" + carId + "_" + partId + "_" + type;
        return key.hashCode();
    }
}
