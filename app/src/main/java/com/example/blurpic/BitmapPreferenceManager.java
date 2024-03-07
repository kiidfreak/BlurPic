package com.example.blurpic;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class BitmapPreferenceManager {

    private static final String PREFERENCE_NAME = "bitmap_preferences";
    private SharedPreferences sharedPreferences;

    public BitmapPreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    public void saveUnblurredBitmap(Bitmap unblurredBitmap) {
        if (unblurredBitmap != null) {
            // Convert the bitmap to a byte array
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            unblurredBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            // Convert the byte array to a Base64 string
            String encodedBitmap = Base64.encodeToString(byteArray, Base64.DEFAULT);

            // Save the Base64 string to SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("unblurred_bitmap", encodedBitmap);
            editor.apply();
        }
    }

    public Bitmap retrieveUnblurredBitmap() {
        // Retrieve the Base64 string from SharedPreferences
        String encodedBitmap = sharedPreferences.getString("unblurred_bitmap", "");

        if (!encodedBitmap.isEmpty()) {
            // Convert the Base64 string to a byte array
            byte[] byteArray = Base64.decode(encodedBitmap, Base64.DEFAULT);

            // Convert the byte array back to a bitmap
            return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        }

        return null;
    }
}
