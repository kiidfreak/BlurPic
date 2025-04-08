package com.example.blurpic;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Calendar;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvDateOfBirth, tvAge;
    private Button btnChangeDob;
    private SharedPreferences sharedPreferences;

    private static final int REQUEST_CODE_OTP = 101; // Unique request code for OTP result

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Set up Toolbar with back button
        Toolbar toolbar = findViewById(R.id.toolbar_profile);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize views
        tvDateOfBirth = findViewById(R.id.tvDateOfBirth);
        tvAge = findViewById(R.id.tvAge);
        btnChangeDob = findViewById(R.id.btnChangeDob);

        sharedPreferences = getSharedPreferences("userPrefs", MODE_PRIVATE);

        // Load saved data
        loadProfileData();

        // Set up change DOB button listener to start OTP flow first
        btnChangeDob.setOnClickListener(v -> startOtpVerification());
    }

    private void startOtpVerification() {
        String savedPhone = sharedPreferences.getString("phone", "");
        if (savedPhone.isEmpty()) {
            Toast.makeText(this, "Phone number not set. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(ProfileActivity.this, LoginOTP.class);
        intent.putExtra("phone", savedPhone);
        intent.putExtra("purpose", "deblur"); // Just a tag to reuse OTP logic
        startActivityForResult(intent, REQUEST_CODE_OTP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_OTP && resultCode == RESULT_OK) {
            boolean verified = data != null && data.getBooleanExtra("verified", false);
            if (verified) {
                openDatePicker(); // Proceed only after OTP
            } else {
                Toast.makeText(this, "Verification failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadProfileData() {
        String savedDob = sharedPreferences.getString("dateOfBirth", "");
        if (!savedDob.isEmpty()) {
            tvDateOfBirth.setText(savedDob);
            calculateAndDisplayAge(savedDob);
        }
    }

    private void calculateAndDisplayAge(String dateOfBirth) {
        String[] dobParts = dateOfBirth.split("-");
        int birthYear = Integer.parseInt(dobParts[0]);
        int birthMonth = Integer.parseInt(dobParts[1]);
        int birthDay = Integer.parseInt(dobParts[2]);

        Calendar birthDate = Calendar.getInstance();
        birthDate.set(birthYear, birthMonth - 1, birthDay);

        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR);
        if (today.get(Calendar.MONTH) + 1 < birthMonth ||
                (today.get(Calendar.MONTH) + 1 == birthMonth && today.get(Calendar.DAY_OF_MONTH) < birthDay)) {
            age--;
        }

        tvAge.setText("Age: " + age);
    }

    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                ProfileActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String newDob = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                    tvDateOfBirth.setText(newDob);
                    saveProfileData(newDob);
                    calculateAndDisplayAge(newDob);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void saveProfileData(String newDob) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("dateOfBirth", newDob);
        editor.apply();

        Toast.makeText(this, "Date of Birth updated successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
