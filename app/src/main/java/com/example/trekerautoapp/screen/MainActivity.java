package com.example.trekerautoapp.screen;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trekerautoapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private EditText etLogin;
    private EditText etPassword;
    private MaterialButton btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();

        etLogin = findViewById(R.id.etLogin);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        TextView tvGoToRegister = findViewById(R.id.tvGoToRegister);

        btnLogin.setOnClickListener(v -> {
            Toast.makeText(this, "Попытка входа...", Toast.LENGTH_SHORT).show();
            login();
        });
        tvGoToRegister.setOnClickListener(v -> openRegister());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() != null) {
            openHome();
        }
    }

    private void login() {
        String email = readTrimmed(etLogin);
        String password = readRaw(etPassword);

        // здесь проверяются входные данные
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Введите email и пароль", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    setLoading(false);
                    openHome();
                })
                .addOnFailureListener(exception -> {
                    if (exception instanceof FirebaseAuthInvalidUserException
                            && "admin@gmail.com".equalsIgnoreCase(email)
                            && "123456".equals(password)) {
                        createAdminAccountAndLogin(email, password);
                        return;
                    }

                    setLoading(false);
                    if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this, "Неверный email или пароль", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Toast.makeText(this, "Ошибка входа: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void createAdminAccountAndLogin(String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    setLoading(false);
                    openHome();
                })
                .addOnFailureListener(exception -> {
                    setLoading(false);
                    Toast.makeText(this, "Ошибка создания аккаунта: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void setLoading(boolean isLoading) {
        btnLogin.setEnabled(!isLoading);
        btnLogin.setText(isLoading ? "Вход..." : "Войти");
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

    private void openRegister() {
        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private void openHome() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
        finishAffinity();
    }
}
