package com.example.blurpic;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PasswordActivity extends AppCompatActivity {

    private EditText pinEntry;
    private Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setpass);

        pinEntry = findViewById(R.id.pinEntry);
        nextButton = findViewById(R.id.next_btn);

        // Disable the "Next" button initially
        nextButton.setEnabled(false);

        // Add a TextWatcher to the EditText to enable/disable the "Next" button based on input length
        pinEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                nextButton.setEnabled(charSequence.length() == 4);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        nextButton.setOnClickListener(view -> {
            // Get the 4-digit password from the EditText
            String password = pinEntry.getText().toString();

            // Save the password to Firebase Realtime Database or Firestore under the user's UID
// Get the current user
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();

            if (user != null) {
                // The user is signed in
                String userId = user.getUid();

                // Update the Realtime Database
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
                databaseReference.child(userId).child("pin").setValue(password);

                // Password set successfully, navigate to MainActivity
                Intent intent = new Intent(PasswordActivity.this, pinEntry.class);
                startActivity(intent);
                finish(); // Optional: finish the current activity to prevent going back
            } else {
                // The user is not signed in or an error occurred
                // Handle
            }

        });
    }
}
