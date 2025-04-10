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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvDateOfBirth, tvAge, tvName;
    private EditText etName;
    private Button btnChangeDob, btnSaveName;
    private SharedPreferences sharedPreferences;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Set up Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_profile);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize views
        tvDateOfBirth = findViewById(R.id.tvDateOfBirth);
        tvAge = findViewById(R.id.tvAge);
        tvName = findViewById(R.id.tvName);
        etName = findViewById(R.id.etName);
        btnChangeDob = findViewById(R.id.btnChangeDob);
        btnSaveName = findViewById(R.id.btnSaveName); // Button for saving name

        sharedPreferences = getSharedPreferences("userPrefs", MODE_PRIVATE);
        firebaseDatabase = FirebaseDatabase.getInstance();
        userRef = firebaseDatabase.getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        // Load saved data
        loadProfileData();

        // Set up change DOB button listener
        btnChangeDob.setOnClickListener(v -> openDatePicker());

        // Set up save name button listener
        btnSaveName.setOnClickListener(v -> saveName());
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
                    saveProfileData(newDob); // Save date of birth
                    calculateAndDisplayAge(newDob); // Update age
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void loadProfileData() {
        String savedName = sharedPreferences.getString("name", "");
        if (!savedName.isEmpty()) {
            etName.setText(savedName); // Display name in the EditText
        }

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

    private void saveProfileData(String newDob) {
        String name = etName.getText().toString().trim();

        if (!name.isEmpty()) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("name", name);
            editor.putString("dateOfBirth", newDob);
            editor.apply();

            // Save to Firebase
            userRef.child("name").setValue(name);
            userRef.child("dateOfBirth").setValue(newDob);

            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveName() {
        String name = etName.getText().toString().trim();
        if (!name.isEmpty()) {
            // Save to shared preferences and Firebase
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("name", name);
            editor.apply();

            userRef.child("name").setValue(name);

            Toast.makeText(this, "Name updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
        }
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
