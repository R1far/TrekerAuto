package com.example.trekerautoapp.data;

import com.example.trekerautoapp.model.ServiceRecord;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

public class ServiceRepository {
    private static final String USERS = "users";
    private static final String CARS = "cars";
    private static final String PARTS = "parts";
    private static final String SERVICE_HISTORY = "serviceHistory";

    private final FirebaseFirestore firestore;

    public ServiceRepository() {
        this(FirebaseFirestore.getInstance());
    }

    ServiceRepository(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    private DocumentReference carDoc(String ownerId, String carId) {
        return firestore.collection(USERS).document(ownerId).collection(CARS).document(carId);
    }

    private DocumentReference partDoc(String ownerId, String carId, String partId) {
        return carDoc(ownerId, carId).collection(PARTS).document(partId);
    }

    private CollectionReference serviceHistory(String ownerId, String carId) {
        return carDoc(ownerId, carId).collection(SERVICE_HISTORY);
    }

    public void addServiceWithPartReset(
            String ownerId,
            String carId,
            long currentCarMileage,
            ServiceRecord serviceRecord,
            OnSuccessListener<Void> onSuccess,
            OnFailureListener onFailure
    ) {
        DocumentReference carRef = carDoc(ownerId, carId);
        DocumentReference serviceRef = serviceHistory(ownerId, carId).document();
        serviceRecord.setId(serviceRef.getId());

        WriteBatch batch = firestore.batch();
        batch.set(serviceRef, serviceRecord);
        batch.update(
                partDoc(ownerId, carId, serviceRecord.getPartId()),
                "lastServiceMileage", serviceRecord.getMileage(),
                "lastServiceMileageKnown", true
        );

        // здесь выполняется проверка пробега
        if (serviceRecord.getMileage() > currentCarMileage) {
            batch.update(carRef, "mileage", serviceRecord.getMileage());
        }

        // здесь сохраняются данные
        batch.commit()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void getServiceHistory(
            String ownerId,
            String carId,
            OnSuccessListener<QuerySnapshot> onSuccess,
            OnFailureListener onFailure
    ) {
        serviceHistory(ownerId, carId)
                .orderBy("serviceDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }
}
