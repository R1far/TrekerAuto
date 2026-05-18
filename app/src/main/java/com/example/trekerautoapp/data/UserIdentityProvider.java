package com.example.trekerautoapp.data;

import android.content.Context;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public final class UserIdentityProvider {
    private UserIdentityProvider() {
    }

    public static String resolveOwnerId(Context context) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return "";
        }

        String userId = currentUser.getUid();
        if (userId == null) {
            return "";
        }

        String cleanUserId = userId.trim();
        // здесь проверяется корректность данных
        if (cleanUserId.isEmpty()) {
            return "";
        }

        return cleanUserId;
    }
}
