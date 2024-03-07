package com.example.blurpic;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText etLoginEmail;
    TextInputEditText etLoginPassword;
    TextView tvRegisterHere;
    Button btnLogin;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPass);
        tvRegisterHere = findViewById(R.id.tvRegisterHere);
        btnLogin = findViewById(R.id.btnLogin);

        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(view -> {
            loginUser();
        });

        tvRegisterHere.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, Registration.class));
        });

        checkAndNavigate(); // Check if the user should be prompted to log in
    }

    private void checkAndNavigate() {
        if (shouldAskForLogin()) {
            // Show the login screen
        } else {
            // Directly go to the main activity
            startActivity(new Intent(LoginActivity.this, pinEntry.class));
            finish();
        }
    }

    private void loginUser() {
        String email = etLoginEmail.getText().toString();
        String password = etLoginPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            etLoginEmail.setError("Email cannot be empty");
            etLoginEmail.requestFocus();
        } else if (TextUtils.isEmpty(password)) {
            etLoginPassword.setError("Password cannot be empty");
            etLoginPassword.requestFocus();
        } else {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        saveLastLoginTimestamp(); // Save the timestamp when the user last logged in
                        startActivity(new Intent(LoginActivity.this, PhoneNumber.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Log in Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void saveLastLoginTimestamp() {
        long timestamp = Calendar.getInstance().getTimeInMillis();
        getSharedPreferences("MyPrefs", MODE_PRIVATE).edit().putLong("lastLoginTimestamp", timestamp).apply();
    }

    private boolean shouldAskForLogin() {
        long lastLoginTimestamp = getSharedPreferences("MyPrefs", MODE_PRIVATE).getLong("lastLoginTimestamp", 0);
        long currentTime = Calendar.getInstance().getTimeInMillis();
        long elapsedTime = currentTime - lastLoginTimestamp;

        // Ask for login if more than 48 hours have passed or no timestamp is available
        return elapsedTime > (48 * 60 * 60 * 1000) || lastLoginTimestamp == 0;
    }
}
