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

public class Registration extends AppCompatActivity {

    TextInputEditText etRegEmail;
    TextInputEditText etRegPassword;
    TextView tvLoginHere;
    Button btnRegister;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPass);
        tvLoginHere = findViewById(R.id.tvLoginHere);
        btnRegister = findViewById(R.id.btnRegister);

        mAuth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(view ->{
            createUser();
        });

        tvLoginHere.setOnClickListener(view ->{
            startActivity(new Intent(Registration.this, LoginActivity.class));
        });

        checkAndNavigate(); // Check if the user should be prompted to register
    }

    private void checkAndNavigate() {
        if (shouldAskForRegistration()) {
            // Show the registration screen
        } else {
            // Directly go to the login activity
            startActivity(new Intent(Registration.this, LoginActivity.class));
            finish();
        }
    }

    private void createUser() {
        String email = etRegEmail.getText().toString();
        String password = etRegPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            etRegEmail.setError("Email cannot be empty");
            etRegEmail.requestFocus();
        } else if (TextUtils.isEmpty(password)) {
            etRegPassword.setError("Password cannot be empty");
            etRegPassword.requestFocus();
        } else {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        saveLastRegistrationTimestamp(); // Save the timestamp when the user last registered
                        Toast.makeText(Registration.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Registration.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(Registration.this, "Registration Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void saveLastRegistrationTimestamp() {
        long timestamp = Calendar.getInstance().getTimeInMillis();
        getSharedPreferences("MyPrefs", MODE_PRIVATE).edit().putLong("lastRegistrationTimestamp", timestamp).apply();
    }

    private boolean shouldAskForRegistration() {
        long lastRegistrationTimestamp = getSharedPreferences("MyPrefs", MODE_PRIVATE).getLong("lastRegistrationTimestamp", 0);
        long currentTime = Calendar.getInstance().getTimeInMillis();
        long elapsedTime = currentTime - lastRegistrationTimestamp;

        // Ask for registration if more than 48 hours have passed or no timestamp is available
        return elapsedTime > (48 * 60 * 60 * 1000) || lastRegistrationTimestamp == 0;
    }
}
