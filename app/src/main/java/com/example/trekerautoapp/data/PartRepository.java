package com.example.trekerautoapp.data;

import com.example.trekerautoapp.model.PartItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class PartRepository {
    private static final String USERS = "users";
    private static final String CARS = "cars";
    private static final String PARTS = "parts";

    private final FirebaseFirestore firestore;

    public PartRepository() {
        this(FirebaseFirestore.getInstance());
    }

    PartRepository(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    private DocumentReference carDoc(String ownerId, String carId) {
        return firestore.collection(USERS).document(ownerId).collection(CARS).document(carId);
    }

    private CollectionReference parts(String ownerId, String carId) {
        return carDoc(ownerId, carId).collection(PARTS);
    }

    private DocumentReference partDoc(String ownerId, String carId, String partId) {
        return parts(ownerId, carId).document(partId);
    }

    public void addPart(
            String ownerId,
            String carId,
            PartItem partItem,
            OnSuccessListener<DocumentReference> onSuccess,
            OnFailureListener onFailure
    ) {
        parts(ownerId, carId)
                .add(partItem)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void getParts(
            String ownerId,
            String carId,
            OnSuccessListener<QuerySnapshot> onSuccess,
            OnFailureListener onFailure
    ) {
        parts(ownerId, carId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void updatePartLastServiceMileage(
            String ownerId,
            String carId,
            String partId,
            long lastServiceMileage,
            OnSuccessListener<Void> onSuccess,
            OnFailureListener onFailure
    ) {
        // здесь сохраняются данные
        partDoc(ownerId, carId, partId)
                .update(
                        "lastServiceMileage", lastServiceMileage,
                        "lastServiceMileageKnown", true
                )
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void getPart(
            String ownerId,
            String carId,
            String partId,
            OnSuccessListener<DocumentSnapshot> onSuccess,
            OnFailureListener onFailure
    ) {
        partDoc(ownerId, carId, partId)
                .get()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void updatePart(
            String ownerId,
            String carId,
            String partId,
            String name,
            String controlType,
            long intervalKm,
            long lastServiceMileage,
            boolean lastServiceMileageKnown,
            OnSuccessListener<Void> onSuccess,
            OnFailureListener onFailure
    ) {
        // здесь сохраняются данные
        partDoc(ownerId, carId, partId)
                .update(
                        "name", name,
                        "controlType", controlType,
                        "intervalKm", intervalKm,
                        "lastServiceMileage", lastServiceMileage,
                        "lastServiceMileageKnown", lastServiceMileageKnown
                )
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void deletePart(
            String ownerId,
            String carId,
            String partId,
            OnSuccessListener<Void> onSuccess,
            OnFailureListener onFailure
    ) {
        partDoc(ownerId, carId, partId)
                .delete()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }
}
