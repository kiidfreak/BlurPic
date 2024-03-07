package com.example.blurpic;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class pinEntryGallery extends AppCompatActivity {

    private static final String TAG = "pinEntryGallery";

    TextInputEditText etPinEntry;
    Button btnSubmit;
    FirebaseAuth mAuth;

    Bitmap blurredImageBitmap;
    Bitmap unblurredBitmap; // Add this variable to store the unblurred bitmap

    // Add this at the beginning of your pinEntryGallery activity
    private BitmapPreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_entry);

        preferenceManager = new BitmapPreferenceManager(this);

        etPinEntry = findViewById(R.id.pinEntry);
        btnSubmit = findViewById(R.id.btnSubmit);

        mAuth = FirebaseAuth.getInstance();

        byte[] byteArray = getIntent().getByteArrayExtra("IMAGE_BITMAP");
        blurredImageBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        // Retrieve the unblurred bitmap using your existing logic
        unblurredBitmap = getIntent().getParcelableExtra("UNBLURRED_IMAGE_BITMAP");

        if (unblurredBitmap != null) {
            Log.d(TAG, "Unblurred Bitmap dimensions: " + unblurredBitmap.getWidth() + " x " + unblurredBitmap.getHeight());
        } else {
            Log.e(TAG, "Unblurred Bitmap is null");
        }

        // Move the log statements here after unblurredBitmap has been assigned a value
        Log.d(TAG, "Blurred Bitmap dimensions: " + blurredImageBitmap.getWidth() + " x " + blurredImageBitmap.getHeight());
        if (unblurredBitmap != null) {
            Log.d(TAG, "Unblurred Bitmap dimensions: " + unblurredBitmap.getWidth() + " x " + unblurredBitmap.getHeight());
        } else {
            Log.e(TAG, "Unblurred Bitmap is null");
        }

        btnSubmit.setOnClickListener(view -> {
            verifyPin(blurredImageBitmap, unblurredBitmap);
        });
    }

    private void verifyPin(Bitmap blurredImageBitmap, Bitmap unblurredBitmap) {
        String enteredPin = etPinEntry.getText().toString();

        // Retrieve the user's PIN from Firebase or your chosen storage
        getSavedPinFromFirebase(enteredPin, blurredImageBitmap, unblurredBitmap);
    }

    // Modify the method to accept the unblurred bitmap
    private void getSavedPinFromFirebase(String enteredPin, Bitmap blurredImageBitmap, Bitmap unblurredBitmap) {
        // Get the current user
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        // Reference to the "pin" field under the user's UID in the Realtime Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("pin");

        // Retrieve the saved PIN from Firebase
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String savedPin = dataSnapshot.getValue(String.class);

                    // Compare the entered PIN with the saved PIN
                    if (!TextUtils.isEmpty(savedPin) && enteredPin.equals(savedPin)) {
                        // PIN is correct, proceed to the desired activity (e.g., ImageDetail)
                        if (blurredImageBitmap != null && unblurredBitmap != null) {
                            // Save the unblurred bitmap to SharedPreferences
                            preferenceManager.saveUnblurredBitmap(unblurredBitmap);

                            // Proceed to the ImageDetail activity
                            Intent imageDetailIntent = new Intent(pinEntryGallery.this, ImageDetail.class);

                            // Pass bitmaps using intent extras
                            imageDetailIntent.putExtra("BLURRED_IMAGE_BITMAP", blurredImageBitmap);
                            imageDetailIntent.putExtra("UNBLURRED_IMAGE_BITMAP", unblurredBitmap);

                            startActivity(imageDetailIntent);
                            finish(); // Close the current activity
                        } else {
                            Log.e(TAG, "Blurred or Unblurred Image Bitmap is null");
                            Toast.makeText(pinEntryGallery.this, "Null Bitmap", Toast.LENGTH_SHORT).show();
                            // Handle the case where the blurred or unblurred image bitmap is null
                        }
                    } else {
                        Toast.makeText(pinEntryGallery.this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                        // You might want to implement logic for handling incorrect PIN attempts
                    }
                } else {
                    // Handle the case when the "pin" field doesn't exist in the database
                    Toast.makeText(pinEntryGallery.this, "PIN not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors in reading from the database
                Toast.makeText(pinEntryGallery.this, "Error retrieving PIN", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
