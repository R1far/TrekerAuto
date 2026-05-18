package com.example.trekerautoapp.screen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trekerautoapp.R;
import com.example.trekerautoapp.data.CarRepository;
import com.example.trekerautoapp.data.ServiceRepository;
import com.example.trekerautoapp.data.UserIdentityProvider;
import com.example.trekerautoapp.model.Car;
import com.example.trekerautoapp.model.ServiceRecord;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ServiceHistoryActivity extends AppCompatActivity {
    public static final String EXTRA_CAR_TITLE = "extra_car_title";
    public static final String EXTRA_CAR_META = "extra_car_meta";

    private TextView tvServiceHistoryCarInfo;
    private TextView tvServiceSummaryTitle;
    private TextView tvServiceSummaryMeta;
    private TextView tvHistoryEmpty;
    private LinearLayout serviceHistoryContainer;
    private ProgressBar progressServiceHistory;
    private MaterialButton btnAddService;

    private ServiceRepository serviceRepository;
    private CarRepository carRepository;

    private String ownerId;
    private String carId;
    private long currentMileage;
    private String carTitle;
    private String carMeta;

    private final NumberFormat integerFormat = NumberFormat.getIntegerInstance(new Locale("ru", "RU"));
    private final DecimalFormat costFormat = new DecimalFormat("#0.##");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_history);

        ImageView btnBack = findViewById(R.id.btnServiceHistoryBack);
        btnAddService = findViewById(R.id.btnAddServiceFromHistory);

        tvServiceHistoryCarInfo = findViewById(R.id.tvServiceHistoryCarInfo);
        tvServiceSummaryTitle = findViewById(R.id.tvServiceSummaryTitle);
        tvServiceSummaryMeta = findViewById(R.id.tvServiceSummaryMeta);
        tvHistoryEmpty = findViewById(R.id.tvHistoryEmpty);
        serviceHistoryContainer = findViewById(R.id.serviceHistoryContainer);
        progressServiceHistory = findViewById(R.id.progressServiceHistory);

        serviceRepository = new ServiceRepository();
        carRepository = new CarRepository();

        ownerId = getIntent().getStringExtra(HomeActivity.EXTRA_OWNER_ID);
        carId = getIntent().getStringExtra(HomeActivity.EXTRA_CAR_ID);
        currentMileage = getIntent().getLongExtra(HomeActivity.EXTRA_CAR_MILEAGE, 0L);
        carTitle = getIntent().getStringExtra(EXTRA_CAR_TITLE);
        carMeta = getIntent().getStringExtra(EXTRA_CAR_META);

        tvServiceHistoryCarInfo.setText(buildCarInfoText());
        tvServiceSummaryTitle.setText("0 записей обслуживания");
        tvServiceSummaryMeta.setText("Добавьте первую запись по работам с расходниками.");

        btnBack.setOnClickListener(v -> finish());
        btnAddService.setOnClickListener(v -> openAddServiceScreen());
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ownerId == null || ownerId.trim().isEmpty()) {
            ownerId = UserIdentityProvider.resolveOwnerId(this);
        }
        if (ownerId == null || ownerId.trim().isEmpty() || carId == null || carId.trim().isEmpty()) {
            Toast.makeText(this, "Не удалось открыть историю обслуживания", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadCarInfo();
        loadServiceHistory();
    }

    private void openAddServiceScreen() {
        Intent intent = new Intent(ServiceHistoryActivity.this, AddServiceActivity.class);
        intent.putExtra(HomeActivity.EXTRA_OWNER_ID, ownerId);
        intent.putExtra(HomeActivity.EXTRA_CAR_ID, carId);
        intent.putExtra(HomeActivity.EXTRA_CAR_MILEAGE, currentMileage);
        startActivity(intent);
    }

    private void loadCarInfo() {
        carRepository.getCar(
                ownerId,
                carId,
                snapshot -> {
                    Car car = snapshot.toObject(Car.class);
                    if (car == null) {
                        return;
                    }
                    currentMileage = car.getMileage();
                    carTitle = (car.getBrand() + " " + car.getModel()).trim();
                    carMeta = car.getPlate() + " • " + car.getYear() + " • " + formatMileage(car.getMileage()) + " км";
                    tvServiceHistoryCarInfo.setText(buildCarInfoText());
                },
                exception -> Toast.makeText(this, "Ошибка загрузки авто: " + exception.getMessage(), Toast.LENGTH_LONG).show()
        );
    }

    private void loadServiceHistory() {
        setLoading(true);
        tvHistoryEmpty.setVisibility(View.GONE);
        serviceHistoryContainer.removeAllViews();

        serviceRepository.getServiceHistory(
                ownerId,
                carId,
                querySnapshot -> {
                    setLoading(false);

                    List<ServiceRecord> records = new ArrayList<>();
                    for (DocumentSnapshot snapshot : querySnapshot.getDocuments()) {
                        ServiceRecord record = snapshot.toObject(ServiceRecord.class);
                        if (record == null) {
                            continue;
                        }
                        record.setId(snapshot.getId());
                        records.add(record);
                    }

                    updateSummary(records);
                    if (records.isEmpty()) {
                        tvHistoryEmpty.setVisibility(View.VISIBLE);
                        return;
                    }

                    for (ServiceRecord record : records) {
                        addRecordCard(record);
                    }
                },
                exception -> {
                    setLoading(false);
                    tvHistoryEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Ошибка загрузки истории: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                }
        );
    }

    private void addRecordCard(ServiceRecord record) {
        View card = getLayoutInflater().inflate(R.layout.item_service_record, serviceHistoryContainer, false);

        TextView tvPartName = card.findViewById(R.id.tvServicePartName);
        TextView tvAction = card.findViewById(R.id.tvServiceAction);
        TextView tvDate = card.findViewById(R.id.tvServiceDate);
        TextView tvMileage = card.findViewById(R.id.tvServiceMileage);
        TextView tvCost = card.findViewById(R.id.tvServiceCost);
        TextView tvComment = card.findViewById(R.id.tvServiceComment);

        tvPartName.setText(record.getPartName());
        tvAction.setText(record.getActionType());
        tvDate.setText(record.getServiceDateText().isEmpty() ? "-" : record.getServiceDateText());
        tvMileage.setText("Пробег: " + formatMileage(record.getMileage()) + " км");

        if (record.getCost() > 0D) {
            tvCost.setVisibility(View.VISIBLE);
            tvCost.setText("Стоимость: " + costFormat.format(record.getCost()) + " ₽");
        } else {
            tvCost.setVisibility(View.GONE);
        }

        if (record.getComment().isEmpty()) {
            tvComment.setVisibility(View.GONE);
        } else {
            tvComment.setVisibility(View.VISIBLE);
            tvComment.setText("Комментарий: " + record.getComment());
        }

        serviceHistoryContainer.addView(card);
    }

    private void updateSummary(List<ServiceRecord> records) {
        int count = records.size();
        String recordsWord = resolveRecordsWord(count);
        tvServiceSummaryTitle.setText(count + " " + recordsWord + " обслуживания");

        if (records.isEmpty()) {
            tvServiceSummaryMeta.setText("Добавьте первую запись по работам с расходниками.");
            return;
        }

        ServiceRecord latestRecord = records.get(0);
        tvServiceSummaryMeta.setText(
                "Последняя запись: "
                        + latestRecord.getServiceDateText()
                        + " • "
                        + formatMileage(latestRecord.getMileage())
                        + " км"
        );
    }

    private void setLoading(boolean isLoading) {
        progressServiceHistory.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private String buildCarInfoText() {
        String safeTitle = carTitle == null ? "" : carTitle.trim();
        String safeMeta = carMeta == null ? "" : carMeta.trim();
        if (!safeTitle.isEmpty() && !safeMeta.isEmpty()) {
            return safeTitle + " • " + safeMeta;
        }
        if (!safeTitle.isEmpty()) {
            return safeTitle;
        }
        if (!safeMeta.isEmpty()) {
            return safeMeta;
        }
        return "Автомобиль";
    }

    private String formatMileage(long mileage) {
        return integerFormat.format(mileage);
    }

    private String resolveRecordsWord(int count) {
        int value = Math.abs(count) % 100;
        int num = value % 10;
        if (value > 10 && value < 20) {
            return "записей";
        }
        if (num > 1 && num < 5) {
            return "записи";
        }
        if (num == 1) {
            return "запись";
        }
        return "записей";
    }
}
