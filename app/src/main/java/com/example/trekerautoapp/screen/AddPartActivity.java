package com.example.trekerautoapp.screen;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.trekerautoapp.R;
import com.example.trekerautoapp.data.PartRepository;
import com.example.trekerautoapp.data.UserIdentityProvider;
import com.example.trekerautoapp.model.PartItem;
import com.example.trekerautoapp.util.PartControlTypeHelper;
import com.example.trekerautoapp.util.PartIntervalDefaults;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;

public class AddPartActivity extends AppCompatActivity {
    private EditText etPartName;
    private MaterialAutoCompleteTextView spPartControlType;
    private EditText etPartInterval;
    private EditText etPartLastServiceMileage;
    private TextInputLayout tilPartInterval;
    private MaterialButton btnSave;

    private PartRepository partRepository;
    private String ownerId;
    private String carId;
    private long currentMileage;
    private String defaultControlType;
    private long suggestedIntervalKm = PartIntervalDefaults.defaultAverageIntervalKm();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_part);

        ImageView btnBack = findViewById(R.id.btnAddPartBack);
        MaterialButton btnCancel = findViewById(R.id.btnCancelAddPart);
        btnSave = findViewById(R.id.btnSavePartStub);

        etPartName = findViewById(R.id.etPartName);
        spPartControlType = findViewById(R.id.spPartControlType);
        etPartInterval = findViewById(R.id.etPartInterval);
        etPartLastServiceMileage = findViewById(R.id.etPartLastServiceMileage);
        tilPartInterval = findViewById(R.id.tilPartInterval);

        ownerId = getIntent().getStringExtra(HomeActivity.EXTRA_OWNER_ID);
        carId = getIntent().getStringExtra(HomeActivity.EXTRA_CAR_ID);
        currentMileage = getIntent().getLongExtra(HomeActivity.EXTRA_CAR_MILEAGE, 0L);
        partRepository = new PartRepository();

        spPartControlType.setSimpleItems(PartControlTypeHelper.options(this));
        defaultControlType = PartControlTypeHelper.defaultValue(this);
        if (spPartControlType.getText() == null || spPartControlType.getText().toString().trim().isEmpty()) {
            spPartControlType.setText(defaultControlType, false);
        }

        etPartLastServiceMileage.setText("");
        updateIntervalHint();
        loadAverageIntervalFromExistingParts();

        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> savePart());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void savePart() {
        // здесь проверяются входные данные
        if (ownerId == null || ownerId.trim().isEmpty()) {
            ownerId = UserIdentityProvider.resolveOwnerId(this);
        }
        if (ownerId == null || ownerId.trim().isEmpty() || carId == null || carId.trim().isEmpty()) {
            Toast.makeText(this, "Не удалось определить автомобиль", Toast.LENGTH_SHORT).show();
            return;
        }

        String partName = safeText(etPartName);
        String controlType = PartControlTypeHelper.normalize(this, safeText(spPartControlType));
        String intervalRaw = safeText(etPartInterval);
        String lastMileageRaw = safeText(etPartLastServiceMileage);
        boolean useSuggestedInterval = intervalRaw.isEmpty();
        boolean hasCustomLastServiceMileage = !lastMileageRaw.isEmpty();

        if (partName.isEmpty()) {
            Toast.makeText(this, "Заполните обязательные поля", Toast.LENGTH_SHORT).show();
            return;
        }

        long intervalKm;
        long lastServiceMileage;
        boolean lastServiceMileageKnown = true;

        try {
            if (useSuggestedInterval) {
                intervalKm = suggestedIntervalKm;
            } else {
                intervalKm = Long.parseLong(intervalRaw);
            }

            if (hasCustomLastServiceMileage) {
                lastServiceMileage = Long.parseLong(lastMileageRaw);
            } else {
                lastServiceMileage = currentMileage;
            }
        } catch (NumberFormatException exception) {
            Toast.makeText(this, "Пробег и интервал должны быть числами", Toast.LENGTH_SHORT).show();
            return;
        }

        if (intervalKm <= 0L) {
            Toast.makeText(this, "Интервал должен быть больше 0", Toast.LENGTH_SHORT).show();
            return;
        }
        if (lastServiceMileage < 0L) {
            Toast.makeText(this, "Пробег обслуживания не может быть отрицательным", Toast.LENGTH_SHORT).show();
            return;
        }

        PartItem partItem = new PartItem(
                partName,
                controlType,
                intervalKm,
                lastServiceMileage,
                lastServiceMileageKnown,
                System.currentTimeMillis()
        );

        btnSave.setEnabled(false);
        partRepository.addPart(
                ownerId,
                carId,
                partItem,
                documentReference -> {
                    // здесь сохраняются данные
                    String message = buildSavedMessage(
                            useSuggestedInterval,
                            hasCustomLastServiceMileage,
                            intervalKm
                    );
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                },
                exception -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Ошибка сохранения: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                }
        );
    }

    private String buildSavedMessage(boolean useSuggestedInterval, boolean hasCustomLastMileage, long intervalKm) {
        if (useSuggestedInterval && !hasCustomLastMileage) {
            return "Запчасть сохранена. Интервал: " + intervalKm + " км, пробег обслуживания: " + currentMileage + " км";
        }
        if (useSuggestedInterval) {
            return "Запчасть сохранена. Применен средний интервал: " + intervalKm + " км";
        }
        if (!hasCustomLastMileage) {
            return "Запчасть сохранена. Пробег обслуживания: " + currentMileage + " км";
        }
        return "Запчасть сохранена";
    }

    private void loadAverageIntervalFromExistingParts() {
        if (ownerId == null || ownerId.trim().isEmpty()) {
            ownerId = UserIdentityProvider.resolveOwnerId(this);
        }
        if (ownerId == null || ownerId.trim().isEmpty() || carId == null || carId.trim().isEmpty()) {
            return;
        }

        partRepository.getParts(
                ownerId,
                carId,
                querySnapshot -> {
                    long sum = 0L;
                    long count = 0L;

                    for (DocumentSnapshot snapshot : querySnapshot.getDocuments()) {
                        PartItem item = snapshot.toObject(PartItem.class);
                        if (item == null) {
                            continue;
                        }

                        long interval = item.getIntervalKm();
                        if (interval > 0L) {
                            sum += interval;
                            count++;
                        }
                    }

                    if (count > 0L) {
                        suggestedIntervalKm = Math.max(1L, Math.round((double) sum / (double) count));
                        updateIntervalHint();
                    }
                },
                unused -> {
                }
        );
    }

    private void updateIntervalHint() {
        etPartInterval.setHint("По умолчанию: " + suggestedIntervalKm + " км");
        if (tilPartInterval != null) {
            tilPartInterval.setHelperText("Средний интервал: " + suggestedIntervalKm + " км");
        }
    }

    private String safeText(EditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }
}
