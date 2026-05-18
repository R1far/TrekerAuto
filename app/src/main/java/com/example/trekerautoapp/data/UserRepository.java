package com.example.trekerautoapp.data;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {
    private static final String USERS = "users";

    private final FirebaseFirestore firestore;

    public UserRepository() {
        this(FirebaseFirestore.getInstance());
    }

    UserRepository(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    private DocumentReference userDoc(String userId) {
        return firestore.collection(USERS).document(userId);
    }

    public void upsertUserProfile(
            String userId,
            String name,
            String email,
            OnSuccessListener<Void> onSuccess,
            OnFailureListener onFailure
    ) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("updatedAt", FieldValue.serverTimestamp());
        userData.put("createdAt", FieldValue.serverTimestamp());

        // здесь сохраняются данные
        userDoc(userId)
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }
}
