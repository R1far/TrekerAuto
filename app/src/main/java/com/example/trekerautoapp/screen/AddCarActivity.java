package com.example.trekerautoapp.screen;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.trekerautoapp.R;
import com.example.trekerautoapp.data.CarRepository;
import com.example.trekerautoapp.data.UserIdentityProvider;
import com.example.trekerautoapp.model.Car;
import com.google.android.material.button.MaterialButton;

public class AddCarActivity extends AppCompatActivity {
    private EditText etCarBrand;
    private EditText etCarModel;
    private EditText etCarYear;
    private EditText etCarPlate;
    private EditText etCarMileage;
    private MaterialButton btnSave;
    private CarRepository carRepository;
    private String ownerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car);

        ImageView btnBack = findViewById(R.id.btnAddCarBack);
        MaterialButton btnCancel = findViewById(R.id.btnCancelAddCar);
        btnSave = findViewById(R.id.btnSaveCarStub);

        etCarBrand = findViewById(R.id.etCarBrand);
        etCarModel = findViewById(R.id.etCarModel);
        etCarYear = findViewById(R.id.etCarYear);
        etCarPlate = findViewById(R.id.etCarPlate);
        etCarMileage = findViewById(R.id.etCarMileage);

        carRepository = new CarRepository();
        ownerId = UserIdentityProvider.resolveOwnerId(this);

        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveCar());
    }

    private void saveCar() {
        if (ownerId == null || ownerId.trim().isEmpty()) {
            Toast.makeText(this, "Авторизация еще выполняется, попробуйте снова", Toast.LENGTH_SHORT).show();
            return;
        }

        String brand = etCarBrand.getText() == null ? "" : etCarBrand.getText().toString().trim();
        String model = etCarModel.getText() == null ? "" : etCarModel.getText().toString().trim();
        String year = etCarYear.getText() == null ? "" : etCarYear.getText().toString().trim();
        String plate = etCarPlate.getText() == null ? "" : etCarPlate.getText().toString().trim().toUpperCase();
        String mileageRaw = etCarMileage.getText() == null ? "" : etCarMileage.getText().toString().trim();

        if (brand.isEmpty() || model.isEmpty() || year.isEmpty() || plate.isEmpty() || mileageRaw.isEmpty()) {
            Toast.makeText(this, "Заполните все обязательные поля", Toast.LENGTH_SHORT).show();
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

        Car car = new Car(brand, model, year, plate, mileage, System.currentTimeMillis());

        btnSave.setEnabled(false);
        carRepository.addCar(
                ownerId,
                car,
                documentReference -> {
                    Toast.makeText(this, "Автомобиль сохранен", Toast.LENGTH_SHORT).show();
                    finish();
                },
                exception -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Ошибка сохранения: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                }
        );
    }
}
