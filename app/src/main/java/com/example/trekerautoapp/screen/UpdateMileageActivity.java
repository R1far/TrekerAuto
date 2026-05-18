package com.example.trekerautoapp.screen;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trekerautoapp.R;
import com.example.trekerautoapp.data.CarRepository;
import com.example.trekerautoapp.data.UserIdentityProvider;
import com.google.android.material.button.MaterialButton;

public class UpdateMileageActivity extends AppCompatActivity {
    private EditText etMileageCurrent;
    private MaterialButton btnSave;

    private CarRepository carRepository;
    private String ownerId;
    private String carId;
    private long currentMileage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_mileage);

        ImageView btnBack = findViewById(R.id.btnUpdateMileageBack);
        MaterialButton btnCancel = findViewById(R.id.btnCancelMileage);
        btnSave = findViewById(R.id.btnSaveMileage);
        etMileageCurrent = findViewById(R.id.etMileageCurrent);

        ownerId = getIntent().getStringExtra(HomeActivity.EXTRA_OWNER_ID);
        carId = getIntent().getStringExtra(HomeActivity.EXTRA_CAR_ID);
        currentMileage = getIntent().getLongExtra(HomeActivity.EXTRA_CAR_MILEAGE, 0L);
        etMileageCurrent.setText(String.valueOf(currentMileage));

        carRepository = new CarRepository();

        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveMileage());
    }

    private void saveMileage() {
        if (ownerId == null || ownerId.trim().isEmpty()) {
            ownerId = UserIdentityProvider.resolveOwnerId(this);
        }
        if (ownerId == null || ownerId.trim().isEmpty() || carId == null || carId.trim().isEmpty()) {
            Toast.makeText(this, "Не удалось определить автомобиль", Toast.LENGTH_SHORT).show();
            return;
        }

        String mileageRaw = etMileageCurrent.getText() == null ? "" : etMileageCurrent.getText().toString().trim();
        if (mileageRaw.isEmpty()) {
            Toast.makeText(this, "Введите пробег", Toast.LENGTH_SHORT).show();
            return;
        }

        long mileage;
        try {
            mileage = Long.parseLong(mileageRaw);
        } catch (NumberFormatException exception) {
            Toast.makeText(this, "Пробег должен быть числом", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mileage < 0) {
            Toast.makeText(this, "Пробег не может быть отрицательным", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        carRepository.updateMileage(
                ownerId,
                carId,
                mileage,
                unused -> {
                    Toast.makeText(this, "Пробег обновлен", Toast.LENGTH_SHORT).show();
                    finish();
                },
                exception -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Ошибка сохранения: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                }
        );
    }
}
