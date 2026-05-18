package com.example.trekerautoapp.data;

import com.example.trekerautoapp.model.Car;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class CarRepository {
    private static final String USERS = "users";
    private static final String CARS = "cars";

    private final FirebaseFirestore firestore;

    public CarRepository() {
        this(FirebaseFirestore.getInstance());
    }

    CarRepository(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    private DocumentReference userDoc(String ownerId) {
        return firestore.collection(USERS).document(ownerId);
    }

    private CollectionReference cars(String ownerId) {
        return userDoc(ownerId).collection(CARS);
    }

    private DocumentReference carDoc(String ownerId, String carId) {
        return cars(ownerId).document(carId);
    }

    public void addCar(
            String ownerId,
            Car car,
            OnSuccessListener<DocumentReference> onSuccess,
            OnFailureListener onFailure
    ) {
        cars(ownerId)
                .add(car)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void getCars(
            String ownerId,
            OnSuccessListener<QuerySnapshot> onSuccess,
            OnFailureListener onFailure
    ) {
        cars(ownerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void getCar(
            String ownerId,
            String carId,
            OnSuccessListener<DocumentSnapshot> onSuccess,
            OnFailureListener onFailure
    ) {
        carDoc(ownerId, carId)
                .get()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void updateMileage(
            String ownerId,
            String carId,
            long mileage,
            OnSuccessListener<Void> onSuccess,
            OnFailureListener onFailure
    ) {
        // здесь сохраняются данные
        carDoc(ownerId, carId)
                .update("mileage", mileage)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void deleteCar(
            String ownerId,
            String carId,
            OnSuccessListener<Void> onSuccess,
            OnFailureListener onFailure
    ) {
        carDoc(ownerId, carId)
                .delete()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }
}
