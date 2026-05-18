package com.example.trekerautoapp.screen;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trekerautoapp.R;
import com.example.trekerautoapp.data.PartRepository;
import com.example.trekerautoapp.data.ServiceRepository;
import com.example.trekerautoapp.data.UserIdentityProvider;
import com.example.trekerautoapp.model.PartItem;
import com.example.trekerautoapp.model.ServiceRecord;
import com.example.trekerautoapp.util.PartControlTypeHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddServiceActivity extends AppCompatActivity {
    private MaterialAutoCompleteTextView spServicePart;
    private EditText etServiceDate;
    private EditText etServiceMileage;
    private EditText etServiceComment;
    private EditText etServiceCost;
    private MaterialButton btnSave;

    private PartRepository partRepository;
    private ServiceRepository serviceRepository;

    private String ownerId;
    private String carId;
    private long currentMileage;

    private final List<PartItem> availableParts = new ArrayList<>();
    private final List<String> partOptions = new ArrayList<>();

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_service);

        ImageView btnBack = findViewById(R.id.btnAddServiceBack);
        MaterialButton btnCancel = findViewById(R.id.btnCancelAddService);
        btnSave = findViewById(R.id.btnSaveAddService);

        spServicePart = findViewById(R.id.spServicePart);
        etServiceDate = findViewById(R.id.etServiceDate);
        etServiceMileage = findViewById(R.id.etServiceMileage);
        etServiceComment = findViewById(R.id.etServiceComment);
        etServiceCost = findViewById(R.id.etServiceCost);

        ownerId = getIntent().getStringExtra(HomeActivity.EXTRA_OWNER_ID);
        carId = getIntent().getStringExtra(HomeActivity.EXTRA_CAR_ID);
        currentMileage = getIntent().getLongExtra(HomeActivity.EXTRA_CAR_MILEAGE, 0L);

        partRepository = new PartRepository();
        serviceRepository = new ServiceRepository();
        dateFormat.setLenient(false);

        etServiceDate.setText(dateFormat.format(new Date()));
        etServiceMileage.setText(String.valueOf(currentMileage));

        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveServiceRecord());
        btnSave.setEnabled(false);

        loadParts();
    }

    private void loadParts() {
        // здесь проверяются входные данные
        if (ownerId == null || ownerId.trim().isEmpty()) {
            ownerId = UserIdentityProvider.resolveOwnerId(this);
        }
        if (ownerId == null || ownerId.trim().isEmpty() || carId == null || carId.trim().isEmpty()) {
            Toast.makeText(this, "ÐÐµ ÑƒÐ´Ð°Ð»Ð¾ÑÑŒ Ð¾Ð¿Ñ€ÐµÐ´ÐµÐ»Ð¸Ñ‚ÑŒ Ð°Ð²Ñ‚Ð¾Ð¼Ð¾Ð±Ð¸Ð»ÑŒ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        partRepository.getParts(
                ownerId,
                carId,
                querySnapshot -> {
                    availableParts.clear();
                    partOptions.clear();

                    for (DocumentSnapshot snapshot : querySnapshot.getDocuments()) {
                        PartItem partItem = snapshot.toObject(PartItem.class);
                        if (partItem == null) {
                            continue;
                        }
                        partItem.setId(snapshot.getId());
                        availableParts.add(partItem);
                    }

                    if (availableParts.isEmpty()) {
                        Toast.makeText(this, "Ð¡Ð½Ð°Ñ‡Ð°Ð»Ð° Ð´Ð¾Ð±Ð°Ð²ÑŒÑ‚Ðµ Ñ€Ð°ÑÑ…Ð¾Ð´Ð½Ð¸Ðº Ð¸Ð»Ð¸ Ð·Ð°Ð¿Ñ‡Ð°ÑÑ‚ÑŒ", Toast.LENGTH_LONG).show();
                        btnSave.setEnabled(false);
                        return;
                    }

                    for (int i = 0; i < availableParts.size(); i++) {
                        PartItem partItem = availableParts.get(i);
                        partOptions.add(buildPartOption(partItem));
                    }

                    String[] options = partOptions.toArray(new String[0]);
                    spServicePart.setSimpleItems(options);
                    spServicePart.setText(options[0], false);
                    btnSave.setEnabled(true);
                },
                exception -> {
                    btnSave.setEnabled(false);
                    Toast.makeText(this, "ÐžÑˆÐ¸Ð±ÐºÐ° Ð·Ð°Ð³Ñ€ÑƒÐ·ÐºÐ¸ Ð·Ð°Ð¿Ñ‡Ð°ÑÑ‚ÐµÐ¹: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                }
        );
    }

    private void saveServiceRecord() {
        // здесь проверяется выбор запчасти
        PartItem selectedPart = resolveSelectedPart();
        if (selectedPart == null) {
            Toast.makeText(this, "Ð’Ñ‹Ð±ÐµÑ€Ð¸Ñ‚Ðµ Ñ€Ð°ÑÑ…Ð¾Ð´Ð½Ð¸Ðº Ð¸Ð»Ð¸ Ð·Ð°Ð¿Ñ‡Ð°ÑÑ‚ÑŒ", Toast.LENGTH_SHORT).show();
            return;
        }

        String actionType = PartControlTypeHelper.normalize(this, selectedPart.getControlType());
        String dateText = safeText(etServiceDate);
        String mileageRaw = safeText(etServiceMileage);
        String comment = safeText(etServiceComment);
        String costRaw = safeText(etServiceCost);

        if (dateText.isEmpty() || mileageRaw.isEmpty()) {
            Toast.makeText(this, "Ð—Ð°Ð¿Ð¾Ð»Ð½Ð¸Ñ‚Ðµ Ð¾Ð±ÑÐ·Ð°Ñ‚ÐµÐ»ÑŒÐ½Ñ‹Ðµ Ð¿Ð¾Ð»Ñ", Toast.LENGTH_SHORT).show();
            return;
        }

        long serviceDateMillis;
        try {
            Date parsedDate = dateFormat.parse(dateText);
            if (parsedDate == null) {
                throw new ParseException("invalid date", 0);
            }
            serviceDateMillis = parsedDate.getTime();
        } catch (ParseException exception) {
            Toast.makeText(this, "Ð”Ð°Ñ‚Ð° Ð´Ð¾Ð»Ð¶Ð½Ð° Ð±Ñ‹Ñ‚ÑŒ Ð² Ñ„Ð¾Ñ€Ð¼Ð°Ñ‚Ðµ Ð”Ð”.ÐœÐœ.Ð“Ð“Ð“Ð“", Toast.LENGTH_SHORT).show();
            return;
        }

        long mileage;
        try {
            mileage = Long.parseLong(mileageRaw);
        } catch (NumberFormatException exception) {
            Toast.makeText(this, "ÐŸÑ€Ð¾Ð±ÐµÐ³ Ð´Ð¾Ð»Ð¶ÐµÐ½ Ð±Ñ‹Ñ‚ÑŒ Ñ‡Ð¸ÑÐ»Ð¾Ð¼", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mileage < 0) {
            Toast.makeText(this, "ÐŸÑ€Ð¾Ð±ÐµÐ³ Ð½Ðµ Ð¼Ð¾Ð¶ÐµÑ‚ Ð±Ñ‹Ñ‚ÑŒ Ð¾Ñ‚Ñ€Ð¸Ñ†Ð°Ñ‚ÐµÐ»ÑŒÐ½Ñ‹Ð¼", Toast.LENGTH_SHORT).show();
            return;
        }

        double cost = 0D;
        if (!costRaw.isEmpty()) {
            try {
                cost = Double.parseDouble(costRaw.replace(',', '.'));
            } catch (NumberFormatException exception) {
                Toast.makeText(this, "Ð¡Ñ‚Ð¾Ð¸Ð¼Ð¾ÑÑ‚ÑŒ Ð´Ð¾Ð»Ð¶Ð½Ð° Ð±Ñ‹Ñ‚ÑŒ Ñ‡Ð¸ÑÐ»Ð¾Ð¼", Toast.LENGTH_SHORT).show();
                return;
            }
            if (cost < 0D) {
                Toast.makeText(this, "Ð¡Ñ‚Ð¾Ð¸Ð¼Ð¾ÑÑ‚ÑŒ Ð½Ðµ Ð¼Ð¾Ð¶ÐµÑ‚ Ð±Ñ‹Ñ‚ÑŒ Ð¾Ñ‚Ñ€Ð¸Ñ†Ð°Ñ‚ÐµÐ»ÑŒÐ½Ð¾Ð¹", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        ServiceRecord serviceRecord = new ServiceRecord(
                selectedPart.getId(),
                selectedPart.getName(),
                actionType,
                serviceDateMillis,
                dateText,
                mileage,
                comment,
                cost,
                System.currentTimeMillis()
        );

        btnSave.setEnabled(false);
        // здесь сохраняются данные
        serviceRepository.addServiceWithPartReset(
                ownerId,
                carId,
                currentMileage,
                serviceRecord,
                unused -> {
                    Toast.makeText(this, "ÐžÐ±ÑÐ»ÑƒÐ¶Ð¸Ð²Ð°Ð½Ð¸Ðµ Ð´Ð¾Ð±Ð°Ð²Ð»ÐµÐ½Ð¾", Toast.LENGTH_SHORT).show();
                    finish();
                },
                exception -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "ÐžÑˆÐ¸Ð±ÐºÐ° ÑÐ¾Ñ…Ñ€Ð°Ð½ÐµÐ½Ð¸Ñ: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                }
        );
    }

    private PartItem resolveSelectedPart() {
        String selectedOption = safeText(spServicePart);
        if (selectedOption.isEmpty()) {
            return null;
        }
        for (int i = 0; i < partOptions.size(); i++) {
            if (selectedOption.equals(partOptions.get(i))) {
                return availableParts.get(i);
            }
        }
        return null;
    }

    private String buildPartOption(PartItem partItem) {
        if (partItem.hasKnownLastServiceMileage()) {
            return partItem.getName() + " • интервал " + partItem.getIntervalKm() + " км";
        }
        return partItem.getName() + " • первичная проверка (пробег неизвестен)";
    }

    private String safeText(EditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }
}

