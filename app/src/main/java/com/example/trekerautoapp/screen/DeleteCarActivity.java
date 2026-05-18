package com.example.trekerautoapp.screen;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trekerautoapp.R;
import com.example.trekerautoapp.data.CarRepository;
import com.example.trekerautoapp.data.UserIdentityProvider;
import com.google.android.material.button.MaterialButton;

public class DeleteCarActivity extends AppCompatActivity {
    private MaterialButton btnConfirm;
    private CarRepository carRepository;
    private String ownerId;
    private String carId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_car);

        ImageView btnBack = findViewById(R.id.btnDeleteCarBack);
        MaterialButton btnCancel = findViewById(R.id.btnCancelDeleteCar);
        btnConfirm = findViewById(R.id.btnConfirmDeleteCar);

        ownerId = getIntent().getStringExtra(HomeActivity.EXTRA_OWNER_ID);
        carId = getIntent().getStringExtra(HomeActivity.EXTRA_CAR_ID);
        carRepository = new CarRepository();

        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());
        btnConfirm.setOnClickListener(v -> deleteCar());
    }

    private void deleteCar() {
        if (ownerId == null || ownerId.trim().isEmpty()) {
            ownerId = UserIdentityProvider.resolveOwnerId(this);
        }
        if (ownerId == null || ownerId.trim().isEmpty() || carId == null || carId.trim().isEmpty()) {
            Toast.makeText(this, "Не удалось определить автомобиль", Toast.LENGTH_SHORT).show();
            return;
        }

        btnConfirm.setEnabled(false);
        carRepository.deleteCar(
                ownerId,
                carId,
                unused -> {
                    Toast.makeText(this, "Автомобиль удален", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(DeleteCarActivity.this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                },
                exception -> {
                    btnConfirm.setEnabled(true);
                    Toast.makeText(this, "Ошибка удаления: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                }
        );
    }
}
