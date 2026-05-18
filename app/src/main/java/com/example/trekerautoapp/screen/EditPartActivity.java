package com.example.trekerautoapp.screen;

import android.app.Activity;
import android.app.AlertDialog;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

public class EditPartActivity extends AppCompatActivity {
    public static final String EXTRA_OWNER_ID = "extra_owner_id";
    public static final String EXTRA_CAR_ID = "extra_car_id";
    public static final String EXTRA_PART_ID = "extra_part_id";
    public static final String EXTRA_CAR_MILEAGE = "extra_car_mileage";

    private EditText etPartName;
    private MaterialAutoCompleteTextView spControlType;
    private EditText etPartInterval;
    private EditText etPartMileage;
    private MaterialButton btnSave;
    private MaterialButton btnDelete;

    private PartRepository partRepository;

    private String ownerId;
    private String carId;
    private String partId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_part);

        ImageView btnBack = findViewById(R.id.btnEditPartBack);
        MaterialButton btnCancel = findViewById(R.id.btnCancelEditPart);
        btnSave = findViewById(R.id.btnSaveEditPart);
        btnDelete = findViewById(R.id.btnDeleteEditPart);

        etPartName = findViewById(R.id.etEditPartName);
        spControlType = findViewById(R.id.spEditPartControlType);
        etPartInterval = findViewById(R.id.etEditPartInterval);
        etPartMileage = findViewById(R.id.etEditPartMileage);

        ownerId = getIntent().getStringExtra(EXTRA_OWNER_ID);
        carId = getIntent().getStringExtra(EXTRA_CAR_ID);
        partId = getIntent().getStringExtra(EXTRA_PART_ID);

        partRepository = new PartRepository();

        spControlType.setSimpleItems(PartControlTypeHelper.options(this));
        spControlType.setText(PartControlTypeHelper.defaultValue(this), false);

        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> savePart());
        btnDelete.setOnClickListener(v -> askDeletePart());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        loadPart();
    }

    private void loadPart() {
        if (ownerId == null || ownerId.trim().isEmpty()) {
            ownerId = UserIdentityProvider.resolveOwnerId(this);
        }
        if (ownerId == null || ownerId.trim().isEmpty() || carId == null || carId.trim().isEmpty() || partId == null || partId.trim().isEmpty()) {
            Toast.makeText(this, "Не удалось определить карточку", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setBusy(true);
        partRepository.getPart(
                ownerId,
                carId,
                partId,
                snapshot -> {
                    setBusy(false);
                    PartItem partItem = snapshot.toObject(PartItem.class);
                    if (partItem == null) {
                        Toast.makeText(this, "Карточка не найдена", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    bindPart(partItem);
                },
                exception -> {
                    setBusy(false);
                    Toast.makeText(this, "Ошибка загрузки: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                }
        );
    }

    private void bindPart(PartItem partItem) {
        etPartName.setText(partItem.getName());
        spControlType.setText(PartControlTypeHelper.normalize(this, partItem.getControlType()), false);
        etPartInterval.setText(String.valueOf(partItem.getIntervalKm()));
        if (partItem.hasKnownLastServiceMileage()) {
            etPartMileage.setText(String.valueOf(partItem.getLastServiceMileage()));
        } else {
            etPartMileage.setText("");
        }
    }

    private void savePart() {
        String name = safeText(etPartName);
        String controlType = PartControlTypeHelper.normalize(this, safeText(spControlType));
        String intervalRaw = safeText(etPartInterval);
        String mileageRaw = safeText(etPartMileage);

        if (name.isEmpty() || intervalRaw.isEmpty()) {
            Toast.makeText(this, "Заполните обязательные поля", Toast.LENGTH_SHORT).show();
            return;
        }

        long intervalKm;
        long lastServiceMileage;
        boolean lastServiceMileageKnown = !mileageRaw.isEmpty();
        try {
            intervalKm = Long.parseLong(intervalRaw);
            lastServiceMileage = lastServiceMileageKnown ? Long.parseLong(mileageRaw) : 0L;
        } catch (NumberFormatException exception) {
            Toast.makeText(this, "Интервал и пробег должны быть числами", Toast.LENGTH_SHORT).show();
            return;
        }

        if (intervalKm <= 0L) {
            Toast.makeText(this, "Интервал должен быть больше 0", Toast.LENGTH_SHORT).show();
            return;
        }
        if (lastServiceMileage < 0L) {
            Toast.makeText(this, "Пробег не может быть отрицательным", Toast.LENGTH_SHORT).show();
            return;
        }

        setBusy(true);
        partRepository.updatePart(
                ownerId,
                carId,
                partId,
                name,
                controlType,
                intervalKm,
                lastServiceMileage,
                lastServiceMileageKnown,
                unused -> {
                    Toast.makeText(this, "Карточка обновлена", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                },
                exception -> {
                    setBusy(false);
                    Toast.makeText(this, "Ошибка сохранения: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                }
        );
    }

    private void askDeletePart() {
        new AlertDialog.Builder(this)
                .setTitle("Удалить карточку?")
                .setMessage("Карточка расходника/запчасти будет удалена без возможности восстановления.")
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Удалить", (dialog, which) -> deletePart())
                .show();
    }

    private void deletePart() {
        setBusy(true);
        partRepository.deletePart(
                ownerId,
                carId,
                partId,
                unused -> {
                    Toast.makeText(this, "Карточка удалена", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                },
                exception -> {
                    setBusy(false);
                    Toast.makeText(this, "Ошибка удаления: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                }
        );
    }

    private void setBusy(boolean busy) {
        btnSave.setEnabled(!busy);
        btnDelete.setEnabled(!busy);
    }

    private String safeText(EditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }
}
