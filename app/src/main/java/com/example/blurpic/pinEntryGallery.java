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

    TextInputEditText etPinEntry;
    Button btnSubmit;
    FirebaseAuth mAuth;

    Bitmap blurredImageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_entry);

        etPinEntry = findViewById(R.id.pinEntry);
        btnSubmit = findViewById(R.id.btnSubmit);

        mAuth = FirebaseAuth.getInstance();

        byte[] byteArray = getIntent().getByteArrayExtra("IMAGE_BITMAP");
        blurredImageBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);


        Log.d("pinEntryGallery", "Byte array length: " + (byteArray != null ? byteArray.length : 0));

        if (byteArray != null) {
            blurredImageBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

            if (blurredImageBitmap != null) {
                Log.d("pinEntryGallery", "Bitmap dimensions: " + blurredImageBitmap.getWidth() + " x " + blurredImageBitmap.getHeight());
            } else {
                Log.e("pinEntryGallery", "Bitmap is null after decoding");
            }
        } else {
            Log.e("pinEntryGallery", "Byte array is null");
        }





        btnSubmit.setOnClickListener(view -> {
            verifyPin(blurredImageBitmap);
        });


    }

    private void verifyPin(Bitmap blurredImageBitmap) {
        String enteredPin = etPinEntry.getText().toString();

        // Retrieve the user's PIN from Firebase or your chosen storage
        getSavedPinFromFirebase(enteredPin);
    }

    // Retrieve the user's PIN from Firebase Realtime Database
    private void getSavedPinFromFirebase(String enteredPin) {
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
                        // PIN is correct, proceed to the desired activity (e.g., MainActivity)

                        if (blurredImageBitmap != null) {
                            Intent imageDetailIntent = new Intent(pinEntryGallery.this, ImageDetail.class);
                            imageDetailIntent.putExtra("BLURRED_IMAGE_BITMAP", blurredImageBitmap);
                            startActivity(imageDetailIntent);
                            finish();
                        } else {
                            Log.e("pinEntryGallery", "Blurred Image Bitmap is null");
                            Toast.makeText(pinEntryGallery.this, "Null Bitmap", Toast.LENGTH_SHORT).show();
                            // Handle the case where the blurred image bitmap is null
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
