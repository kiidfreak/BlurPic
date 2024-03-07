package com.example.blurpic;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class splashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // Replace with your splash screen layout

        // Delayed intent to start the pinEntry activity
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(splashScreen.this, pinEntry.class);
            startActivity(intent);
            finish();
        }, 3000); // 3000 milliseconds (adjust as needed)
    }
}
