package com.example.trekerautoapp.screen;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trekerautoapp.R;
import com.example.trekerautoapp.notifications.PartReminderNotifier;
import com.example.trekerautoapp.notifications.PartReminderScheduler;
import com.example.trekerautoapp.notifications.ReminderSettingsStore;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class ReminderSettingsActivity extends AppCompatActivity {
    private EditText etReminderDays;
    private EditText etNoPartsReminderDays;
    private SwitchMaterial swNoPartsReminder;
    private ReminderSettingsStore settingsStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_settings);

        ImageView btnBack = findViewById(R.id.btnReminderSettingsBack);
        MaterialButton btnCancel = findViewById(R.id.btnCancelReminderSettings);
        MaterialButton btnSave = findViewById(R.id.btnSaveReminderSettings);
        MaterialButton btnTest = findViewById(R.id.btnTestReminderNotification);

        etReminderDays = findViewById(R.id.etReminderIntervalDays);
        etNoPartsReminderDays = findViewById(R.id.etNoPartsReminderDays);
        swNoPartsReminder = findViewById(R.id.swNoPartsReminder);

        settingsStore = new ReminderSettingsStore(this);
        etReminderDays.setText(String.valueOf(settingsStore.getReminderIntervalDays()));
        etNoPartsReminderDays.setText(String.valueOf(settingsStore.getNoPartsReminderIntervalDays()));
        swNoPartsReminder.setChecked(settingsStore.isNoPartsReminderEnabled());
        applyNoPartsInputState(swNoPartsReminder.isChecked());

        swNoPartsReminder.setOnCheckedChangeListener((buttonView, isChecked) -> applyNoPartsInputState(isChecked));

        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveSettings());
        btnTest.setOnClickListener(v -> sendTestNotification());
    }

    private void saveSettings() {
        // здесь проверяется корректность данных
        String reminderText = readTrimmed(etReminderDays);
        if (reminderText.isEmpty()) {
            Toast.makeText(this, "Укажите интервал в днях", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer reminderDays = parseDays(reminderText);
        if (reminderDays == null) {
            Toast.makeText(this, "Интервал должен быть числом", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isDaysInRange(reminderDays)) {
            Toast.makeText(
                    this,
                    "Интервал: от " + ReminderSettingsStore.MIN_REMINDER_DAYS + " до " + ReminderSettingsStore.MAX_REMINDER_DAYS + " дней",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        boolean noPartsEnabled = swNoPartsReminder.isChecked();
        String noPartsText = readTrimmed(etNoPartsReminderDays);
        int noPartsDays = settingsStore.getNoPartsReminderIntervalDays();

        if (noPartsEnabled) {
            if (noPartsText.isEmpty()) {
                Toast.makeText(this, "Укажите интервал для уведомлений о пустом списке", Toast.LENGTH_SHORT).show();
                return;
            }

            Integer parsedNoPartsDays = parseDays(noPartsText);
            if (parsedNoPartsDays == null) {
                Toast.makeText(this, "Интервал пустого списка должен быть числом", Toast.LENGTH_SHORT).show();
                return;
            }

            noPartsDays = parsedNoPartsDays;
            if (!isDaysInRange(noPartsDays)) {
                Toast.makeText(
                        this,
                        "Интервал пустого списка: от " + ReminderSettingsStore.MIN_REMINDER_DAYS + " до " + ReminderSettingsStore.MAX_REMINDER_DAYS + " дней",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }
        }

        // здесь сохраняются данные
        settingsStore.setReminderIntervalDays(reminderDays);
        settingsStore.setNoPartsReminderEnabled(noPartsEnabled);
        settingsStore.setNoPartsReminderIntervalDays(noPartsDays);

        PartReminderScheduler.schedule(this);
        Toast.makeText(this, "Настройки уведомлений сохранены", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void sendTestNotification() {
        PartReminderNotifier.ensureChannel(this);
        int notificationId = (int) (System.currentTimeMillis() & 0x7FFFFFFF);
        boolean sent = PartReminderNotifier.sendNotification(
                this,
                notificationId,
                "Тестовое уведомление TrekerAuto",
                "Если вы видите это сообщение, уведомления работают корректно."
        );

        if (sent) {
            // здесь отправляется уведомление
            Toast.makeText(this, "Тестовое уведомление отправлено", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Нет разрешения на уведомления", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isDaysInRange(int days) {
        return days >= ReminderSettingsStore.MIN_REMINDER_DAYS && days <= ReminderSettingsStore.MAX_REMINDER_DAYS;
    }

    private Integer parseDays(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String readTrimmed(EditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    private void applyNoPartsInputState(boolean enabled) {
        etNoPartsReminderDays.setEnabled(enabled);
        etNoPartsReminderDays.setAlpha(enabled ? 1F : 0.5F);
    }
}
