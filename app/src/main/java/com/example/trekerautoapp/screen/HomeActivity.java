package com.example.trekerautoapp.screen;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.trekerautoapp.R;
import com.example.trekerautoapp.data.CarRepository;
import com.example.trekerautoapp.data.UserIdentityProvider;
import com.example.trekerautoapp.model.Car;
import com.example.trekerautoapp.notifications.PartReminderScheduler;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

public class HomeActivity extends AppCompatActivity {
    public static final String EXTRA_CAR_ID = "extra_car_id";
    public static final String EXTRA_OWNER_ID = "extra_owner_id";
    public static final String EXTRA_CAR_MILEAGE = "extra_car_mileage";

    private static final int NOTIFICATION_PERMISSION_REQUEST = 101;

    private LinearLayout carsContainer;
    private TextView tvCarsEmpty;
    private CarRepository carRepository;
    private String ownerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ImageView btnLogout = findViewById(R.id.btnLogout);
        MaterialButton btnAddCar = findViewById(R.id.btnAddCar);
        MaterialButton btnReminderSettings = findViewById(R.id.btnReminderSettings);
        carsContainer = findViewById(R.id.carsContainer);
        tvCarsEmpty = findViewById(R.id.tvCarsEmpty);

        carRepository = new CarRepository();
        PartReminderScheduler.schedule(this);
        requestNotificationPermissionIfNeeded();

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
            finishAffinity();
        });

        btnAddCar.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddCarActivity.class);
            startActivity(intent);
        });

        btnReminderSettings.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ReminderSettingsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ownerId = UserIdentityProvider.resolveOwnerId(this);
        if (ownerId == null || ownerId.trim().isEmpty()) {
            Toast.makeText(this, "Сессия истекла, войдите снова", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        loadCars();
    }

    private void loadCars() {
        carsContainer.removeAllViews();
        tvCarsEmpty.setVisibility(View.GONE);

        carRepository.getCars(
                ownerId,
                querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        tvCarsEmpty.setVisibility(View.VISIBLE);
                        return;
                    }

                    for (DocumentSnapshot snapshot : querySnapshot.getDocuments()) {
                        Car car = snapshot.toObject(Car.class);
                        if (car == null) {
                            continue;
                        }
                        car.setId(snapshot.getId());
                        addCarCard(car);
                    }
                },
                exception -> {
                    tvCarsEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Ошибка загрузки: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                }
        );
    }

    private void addCarCard(Car car) {
        View card = getLayoutInflater().inflate(R.layout.item_home_car, carsContainer, false);

        TextView tvCarTitle = card.findViewById(R.id.tvCarTitle);
        TextView tvCarMeta = card.findViewById(R.id.tvCarMeta);
        TextView tvCarMileage = card.findViewById(R.id.tvCarMileage);
        TextView tvCarStatus = card.findViewById(R.id.tvCarStatus);

        tvCarTitle.setText(car.getBrand() + " " + car.getModel());
        tvCarMeta.setText(car.getPlate() + " • " + car.getYear());
        tvCarMileage.setText("Пробег: " + car.getMileage() + " км");
        tvCarStatus.setText("Добавлен");

        card.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CarDetailsActivity.class);
            intent.putExtra(EXTRA_CAR_ID, car.getId());
            intent.putExtra(EXTRA_OWNER_ID, ownerId);
            intent.putExtra(EXTRA_CAR_MILEAGE, car.getMileage());
            startActivity(intent);
        });

        carsContainer.addView(card);
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                NOTIFICATION_PERMISSION_REQUEST
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != NOTIFICATION_PERMISSION_REQUEST) {
            return;
        }
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Уведомления включены", Toast.LENGTH_SHORT).show();
        }
    }
}
