package com.example.trekerautoapp.screen;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.trekerautoapp.R;
import com.example.trekerautoapp.data.CarRepository;
import com.example.trekerautoapp.data.PartRepository;
import com.example.trekerautoapp.data.UserIdentityProvider;
import com.example.trekerautoapp.model.Car;
import com.example.trekerautoapp.model.PartItem;
import com.example.trekerautoapp.util.PartControlTypeHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;

public class CarDetailsActivity extends AppCompatActivity {
    private TextView tvCarDetailsTitle;
    private TextView tvCarDetailsMeta;
    private TextView tvPartsEmpty;
    private LinearLayout partsContainer;

    private CarRepository carRepository;
    private PartRepository partRepository;

    private String ownerId;
    private String carId;
    private long currentMileage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_details);

        ImageView btnBack = findViewById(R.id.btnBack);
        MaterialButton btnDeleteCar = findViewById(R.id.btnDeleteCar);
        MaterialButton btnUpdateMileage = findViewById(R.id.btnUpdateMileage);
        MaterialButton btnOpenServiceHistory = findViewById(R.id.btnOpenServiceHistory);
        MaterialButton btnAddPart = findViewById(R.id.btnAddPart);

        tvCarDetailsTitle = findViewById(R.id.tvCarDetailsTitle);
        tvCarDetailsMeta = findViewById(R.id.tvCarDetailsMeta);
        tvPartsEmpty = findViewById(R.id.tvPartsEmpty);
        partsContainer = findViewById(R.id.partsContainer);

        carRepository = new CarRepository();
        partRepository = new PartRepository();

        ownerId = getIntent().getStringExtra(HomeActivity.EXTRA_OWNER_ID);
        carId = getIntent().getStringExtra(HomeActivity.EXTRA_CAR_ID);
        currentMileage = getIntent().getLongExtra(HomeActivity.EXTRA_CAR_MILEAGE, 0L);

        btnBack.setOnClickListener(v -> finish());

        btnDeleteCar.setOnClickListener(v -> {
            Intent intent = new Intent(CarDetailsActivity.this, DeleteCarActivity.class);
            intent.putExtra(HomeActivity.EXTRA_OWNER_ID, ownerId);
            intent.putExtra(HomeActivity.EXTRA_CAR_ID, carId);
            startActivity(intent);
        });

        btnUpdateMileage.setOnClickListener(v -> {
            Intent intent = new Intent(CarDetailsActivity.this, UpdateMileageActivity.class);
            intent.putExtra(HomeActivity.EXTRA_OWNER_ID, ownerId);
            intent.putExtra(HomeActivity.EXTRA_CAR_ID, carId);
            intent.putExtra(HomeActivity.EXTRA_CAR_MILEAGE, currentMileage);
            startActivity(intent);
        });

        btnOpenServiceHistory.setOnClickListener(v -> {
            Intent intent = new Intent(CarDetailsActivity.this, ServiceHistoryActivity.class);
            intent.putExtra(HomeActivity.EXTRA_OWNER_ID, ownerId);
            intent.putExtra(HomeActivity.EXTRA_CAR_ID, carId);
            intent.putExtra(HomeActivity.EXTRA_CAR_MILEAGE, currentMileage);
            intent.putExtra(ServiceHistoryActivity.EXTRA_CAR_TITLE, tvCarDetailsTitle.getText().toString());
            intent.putExtra(ServiceHistoryActivity.EXTRA_CAR_META, tvCarDetailsMeta.getText().toString());
            startActivity(intent);
        });

        btnAddPart.setOnClickListener(v -> {
            Intent intent = new Intent(CarDetailsActivity.this, AddPartActivity.class);
            intent.putExtra(HomeActivity.EXTRA_OWNER_ID, ownerId);
            intent.putExtra(HomeActivity.EXTRA_CAR_ID, carId);
            intent.putExtra(HomeActivity.EXTRA_CAR_MILEAGE, currentMileage);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ownerId == null || ownerId.trim().isEmpty()) {
            ownerId = UserIdentityProvider.resolveOwnerId(this);
        }
        if (ownerId == null || ownerId.trim().isEmpty() || carId == null || carId.trim().isEmpty()) {
            Toast.makeText(this, "Не удалось открыть карточку автомобиля", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadCar();
        loadParts();
    }

    private void loadCar() {
        carRepository.getCar(
                ownerId,
                carId,
                this::onCarLoaded,
                exception -> Toast.makeText(this, "Ошибка загрузки авто: " + exception.getMessage(), Toast.LENGTH_LONG).show()
        );
    }

    private void onCarLoaded(DocumentSnapshot snapshot) {
        Car car = snapshot.toObject(Car.class);
        if (car == null) {
            Toast.makeText(this, "Автомобиль не найден", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentMileage = car.getMileage();
        String title = (car.getBrand() + " " + car.getModel()).trim();
        String meta = car.getPlate() + " • " + car.getYear() + " • " + car.getMileage() + " км";

        tvCarDetailsTitle.setText(title);
        tvCarDetailsMeta.setText(meta);
    }

    private void loadParts() {
        partsContainer.removeAllViews();
        tvPartsEmpty.setVisibility(View.GONE);

        partRepository.getParts(
                ownerId,
                carId,
                querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        tvPartsEmpty.setVisibility(View.VISIBLE);
                        return;
                    }

                    for (DocumentSnapshot snapshot : querySnapshot.getDocuments()) {
                        PartItem partItem = snapshot.toObject(PartItem.class);
                        if (partItem == null) {
                            continue;
                        }
                        partItem.setId(snapshot.getId());
                        addPartCard(partItem);
                    }
                },
                exception -> Toast.makeText(this, "Ошибка загрузки запчастей: " + exception.getMessage(), Toast.LENGTH_LONG).show()
        );
    }

    private void addPartCard(PartItem partItem) {
        View card = getLayoutInflater().inflate(R.layout.item_part_card, partsContainer, false);

        TextView tvPartName = card.findViewById(R.id.tvPartName);
        TextView tvPartStatus = card.findViewById(R.id.tvPartStatus);
        TextView tvPartWearDetails = card.findViewById(R.id.tvPartWearDetails);
        ProgressBar progressPartWear = card.findViewById(R.id.progressPartWear);
        MaterialButton btnEditPart = card.findViewById(R.id.btnEditPart);

        tvPartName.setText(partItem.getName());

        if (!partItem.hasKnownLastServiceMileage()) {
            int statusColor = ContextCompat.getColor(this, R.color.home_warning);
            tvPartStatus.setText("Первичная проверка");
            tvPartStatus.setTextColor(statusColor);
            tvPartWearDetails.setText("Пробег последнего обслуживания неизвестен. Выполните первичную проверку.");
            progressPartWear.setProgress(0, true);
            progressPartWear.setProgressTintList(ColorStateList.valueOf(statusColor));
        } else {
            long intervalKm = Math.max(1L, partItem.getIntervalKm());
            long usedKm = Math.max(0L, currentMileage - partItem.getLastServiceMileage());
            long remainingKm = intervalKm - usedKm;
            int wearPercent = (int) Math.min(100L, (usedKm * 100L) / intervalKm);

            int statusColor = resolveStatusColor(wearPercent, remainingKm);
            String statusText = resolveStatusText(partItem, wearPercent, remainingKm);

            tvPartStatus.setText(statusText);
            tvPartStatus.setTextColor(statusColor);
            tvPartWearDetails.setText("Износ: " + wearPercent + "% • осталось " + Math.max(0L, remainingKm) + " км");
            progressPartWear.setProgress(wearPercent, true);
            progressPartWear.setProgressTintList(ColorStateList.valueOf(statusColor));
        }

        btnEditPart.setOnClickListener(v -> {
            Intent intent = new Intent(CarDetailsActivity.this, EditPartActivity.class);
            intent.putExtra(EditPartActivity.EXTRA_OWNER_ID, ownerId);
            intent.putExtra(EditPartActivity.EXTRA_CAR_ID, carId);
            intent.putExtra(EditPartActivity.EXTRA_PART_ID, partItem.getId());
            intent.putExtra(EditPartActivity.EXTRA_CAR_MILEAGE, currentMileage);
            startActivity(intent);
        });

        partsContainer.addView(card);
    }

    private int resolveStatusColor(int wearPercent, long remainingKm) {
        if (remainingKm <= 0) {
            return ContextCompat.getColor(this, R.color.home_danger);
        }
        if (wearPercent >= 75) {
            return ContextCompat.getColor(this, R.color.home_warning);
        }
        if (wearPercent >= 40) {
            return ContextCompat.getColor(this, R.color.home_primary);
        }
        return ContextCompat.getColor(this, R.color.home_good);
    }

    private String resolveStatusText(PartItem partItem, int wearPercent, long remainingKm) {
        if (remainingKm <= 0) {
            String replaceType = PartControlTypeHelper.replaceValue(this);
            String normalizedType = PartControlTypeHelper.normalize(this, partItem.getControlType());
            if (replaceType.equalsIgnoreCase(normalizedType)) {
                return "Нужно заменить";
            }
            return "Нужно проверить";
        }
        if (wearPercent >= 75) {
            return "Скоро проверить";
        }
        if (wearPercent >= 40) {
            return "Плановая проверка";
        }
        return "В норме";
    }
}
