package com.example.blurpic;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ImageDetail extends AppCompatActivity {

    private static final String TAG = "ImageDetailActivity";
    private ImageView imageViewDetail;
    private boolean isBlurred = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        imageViewDetail = findViewById(R.id.imageViewDetail);

        // Retrieve the blurred image item from the intent
        MainActivity.BlurredImage blurredImageItem = getIntent().getParcelableExtra("BLURRED_IMAGE_ITEM");

        // Log the received blurred image item
        Log.d(TAG, "Received BlurredImageItem: " + blurredImageItem);

        // Log the lifecycle event
        Log.d(TAG, "onCreate: ImageDetail");

        if (blurredImageItem != null) {
            Log.d(TAG, "Received BlurredImage: " + blurredImageItem.toString());

            Bitmap blurredImageBitmap = blurredImageItem.getBlurredBitmap();
            if (blurredImageBitmap != null) {
                Log.d(TAG, "Blurred Bitmap dimensions: " + blurredImageBitmap.getWidth() + " x " + blurredImageBitmap.getHeight());

                imageViewDetail.setImageBitmap(blurredImageBitmap);

                // Long-press to toggle between blurred and unblurred
                imageViewDetail.setOnLongClickListener(v -> {
                    if (isBlurred) {
                        // Unblur the image
                        imageViewDetail.setImageBitmap(blurredImageItem.getUnblurredBitmap());
                        isBlurred = false;
                        Toast.makeText(ImageDetail.this, "Image Unblurred", Toast.LENGTH_SHORT).show();
                    } else {
                        // Blur the image again
                        imageViewDetail.setImageBitmap(blurredImageBitmap);
                        isBlurred = true;
                        Toast.makeText(ImageDetail.this, "Image Blurred", Toast.LENGTH_SHORT).show();
                    }
                    return true; // Consume the long click event
                });

                // Set up onTouchListener to detect release
                imageViewDetail.setOnTouchListener((v, event) -> {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        // Handle release event here (e.g., reset to default state)
                        // In this example, revert to blurred image
                        imageViewDetail.setImageBitmap(blurredImageBitmap);
                        isBlurred = true;
                        Toast.makeText(ImageDetail.this, "Image Returned to Blurred State", Toast.LENGTH_SHORT).show();
                        return true; // Consume the touch event
                    }
                    return false;
                });

                // Close button
                findViewById(R.id.btnClose).setOnClickListener(v -> finish());
            } else {
                Log.e(TAG, "Blurred Bitmap is null at Image Detail");
                Toast.makeText(this, "Blurred Bitmap is null at Image Detail", Toast.LENGTH_SHORT).show();
                // Handle the case where the blurred image bitmap is null
                // You might want to add additional handling or notify the user
                finish(); // Finish the activity if the bitmap is null
            }
        } else {
            Log.e(TAG, "BlurredImage item is null at Image Detail");
            Toast.makeText(this, "BlurredImage item is null at Image Detail", Toast.LENGTH_SHORT).show();
            // Handle the case where the blurred image item is null
            // You might want to add additional handling or notify the user
            finish(); // Finish the activity if the blurred image item is null
        }
    }
}
