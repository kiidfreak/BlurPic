package com.example.blurpic;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Calendar;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvDateOfBirth, tvAge;
    private Button btnChangeDob;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Set up Toolbar with back button
        Toolbar toolbar = findViewById(R.id.toolbar_profile);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // Enable back button

        // Initialize views
        tvDateOfBirth = findViewById(R.id.tvDateOfBirth);
        tvAge = findViewById(R.id.tvAge);
        btnChangeDob = findViewById(R.id.btnChangeDob);

        sharedPreferences = getSharedPreferences("userPrefs", MODE_PRIVATE);

        // Load saved data
        loadProfileData();

        // Set up change DOB button listener
        btnChangeDob.setOnClickListener(v -> openDatePicker());
    }

    private void loadProfileData() {
        // Load saved date of birth
        String savedDob = sharedPreferences.getString("dateOfBirth", "");
        if (!savedDob.isEmpty()) {
            tvDateOfBirth.setText(savedDob);
            calculateAndDisplayAge(savedDob);
        }
    }

    private void calculateAndDisplayAge(String dateOfBirth) {
        // Parse the saved date of birth and calculate age
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
            age--; // Subtract one year if birthday hasn't occurred yet this year
        }

        tvAge.setText("Age: " + age);
    }

    private void openDatePicker() {
        // Get the current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Open DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                ProfileActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Save the new date of birth
                    String newDob = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                    tvDateOfBirth.setText(newDob);
                    saveProfileData(newDob);
                    calculateAndDisplayAge(newDob); // Recalculate and display the age
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void saveProfileData(String newDob) {
        // Save the new date of birth to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("dateOfBirth", newDob);
        editor.apply();

        Toast.makeText(this, "Date of Birth updated successfully", Toast.LENGTH_SHORT).show();
    }

    // Handle back button press
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();  // Go back to previous screen
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
