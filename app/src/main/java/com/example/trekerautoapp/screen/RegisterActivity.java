package com.example.trekerautoapp.screen;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trekerautoapp.R;
import com.example.trekerautoapp.data.UserRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private UserRepository userRepository;

    private EditText etRegisterName;
    private EditText etRegisterEmail;
    private EditText etRegisterPassword;
    private EditText etRegisterRepeatPassword;
    private MaterialCheckBox cbAgreement;
    private MaterialButton btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepository();

        etRegisterName = findViewById(R.id.etRegisterName);
        etRegisterEmail = findViewById(R.id.etRegisterEmail);
        etRegisterPassword = findViewById(R.id.etRegisterPassword);
        etRegisterRepeatPassword = findViewById(R.id.etRegisterRepeatPassword);
        cbAgreement = findViewById(R.id.cbAgreement);
        btnRegister = findViewById(R.id.btnRegister);
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnRegister.setOnClickListener(v -> register());
        tvGoToLogin.setOnClickListener(v -> finish());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() != null) {
            openHome();
        }
    }

    private void register() {
        clearFieldErrors();

        String name = readTrimmed(etRegisterName);
        String email = readTrimmed(etRegisterEmail);
        String password = readRaw(etRegisterPassword);
        String repeatPassword = readRaw(etRegisterRepeatPassword);

        // здесь проверяется корректность данных
        if (!isRegisterDataValid(name, email, password, repeatPassword)) {
            return;
        }

        setLoading(true);
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> onRegisterSuccess(authResult.getUser(), name, email))
                .addOnFailureListener(exception -> {
                    setLoading(false);
                    if (exception instanceof FirebaseAuthUserCollisionException) {
                        etRegisterEmail.setError(getString(R.string.register_error_email_exists));
                        etRegisterEmail.requestFocus();
                        return;
                    }
                    if (exception instanceof FirebaseAuthWeakPasswordException) {
                        etRegisterPassword.setError(getString(R.string.register_error_password_short));
                        etRegisterPassword.requestFocus();
                        return;
                    }
                    Toast.makeText(
                            this,
                            getString(R.string.register_error_failed, exception.getMessage()),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private boolean isRegisterDataValid(String name, String email, String password, String repeatPassword) {
        if (name.isEmpty()) {
            etRegisterName.setError(getString(R.string.register_error_name_required));
            etRegisterName.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etRegisterEmail.setError(getString(R.string.register_error_invalid_email));
            etRegisterEmail.requestFocus();
            return false;
        }
        if (password.length() < 6) {
            etRegisterPassword.setError(getString(R.string.register_error_password_short));
            etRegisterPassword.requestFocus();
            return false;
        }
        if (!password.equals(repeatPassword)) {
            etRegisterRepeatPassword.setError(getString(R.string.register_error_password_mismatch));
            etRegisterRepeatPassword.requestFocus();
            return false;
        }
        if (!cbAgreement.isChecked()) {
            Toast.makeText(this, R.string.register_error_agreement_required, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void onRegisterSuccess(FirebaseUser firebaseUser, String name, String email) {
        if (firebaseUser == null || firebaseUser.getUid() == null || firebaseUser.getUid().trim().isEmpty()) {
            setLoading(false);
            Toast.makeText(this, R.string.register_error_failed_generic, Toast.LENGTH_LONG).show();
            return;
        }

        String userId = firebaseUser.getUid().trim();
        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();
        firebaseUser.updateProfile(profileChangeRequest);

        // здесь сохраняются данные
        userRepository.upsertUserProfile(
                userId,
                name,
                email,
                unused -> {
                    setLoading(false);
                    Toast.makeText(this, R.string.register_success, Toast.LENGTH_SHORT).show();
                    openHome();
                },
                exception -> {
                    setLoading(false);
                    Toast.makeText(
                            this,
                            getString(R.string.register_profile_save_error, exception.getMessage()),
                            Toast.LENGTH_LONG
                    ).show();
                    openHome();
                }
        );
    }

    private void setLoading(boolean isLoading) {
        btnRegister.setEnabled(!isLoading);
        btnRegister.setText(isLoading ? R.string.register_button_loading : R.string.register_button);
    }

    private void clearFieldErrors() {
        etRegisterName.setError(null);
        etRegisterEmail.setError(null);
        etRegisterPassword.setError(null);
        etRegisterRepeatPassword.setError(null);
    }

    private String readTrimmed(EditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    private String readRaw(EditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString();
    }

    private void openHome() {
        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
        startActivity(intent);
        finishAffinity();
    }
}
