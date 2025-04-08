package com.example.blurpic;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import jp.wasabeef.glide.transformations.BlurTransformation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Button camera, gallery;
    private ImageView imageView;
    private Bitmap blurredBitmap;
    private SeekBar blurSlider;
    private Bitmap originalBitmap;
    private int blurLevel = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = getSharedPreferences("userPrefs", MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean("isLoggedIn", false);
        String pin = preferences.getString("userPin", null);

        if (!isLoggedIn) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        // Prompt to set PIN if not already set
        if (pin == null) {
            promptSetPin();
        }



        camera = findViewById(R.id.button);
        gallery = findViewById(R.id.button2);
        imageView = findViewById(R.id.imageView);
        blurSlider = findViewById(R.id.blurSlider);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("BlurPic");

        camera.setOnClickListener(view -> requestCameraPermission());

        gallery.setOnClickListener(view -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch("image/*");
        });

        blurSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                blurLevel = progress;
                if (originalBitmap != null && blurLevel > 0) {
                    applyBlur(imageView, originalBitmap, blurLevel);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);  // menu will be defined below
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();


        if (id == R.id.action_save) {
            saveImageToDevice();
            return true;

        } else if (id == R.id.action_reset) {
            resetToOriginalImage();
            return true;

        } else if (id == R.id.action_logout) {
            // Clear login state
            SharedPreferences.Editor editor = getSharedPreferences("userPrefs", MODE_PRIVATE).edit();
            editor.clear();
            editor.apply();

            // Go back to login screen
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear backstack
            startActivity(intent);
            return true;

        } else if (id == R.id.action_my_profile) {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void promptSetPin() {
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setHint("Enter a 4-digit PIN");

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Set PIN")
                .setMessage("Please set a 4-digit PIN for saving images.")
                .setView(input)
                .setCancelable(false)
                .setPositiveButton("Save", (dialog, which) -> {
                    String pin = input.getText().toString();
                    if (pin.length() == 4) {
                        SharedPreferences.Editor editor = getSharedPreferences("userPrefs", MODE_PRIVATE).edit();
                        editor.putString("userPin", pin);
                        editor.apply();
                        Toast.makeText(this, "PIN set successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Please enter a valid 4-digit PIN.", Toast.LENGTH_SHORT).show();
                        promptSetPin();
                    }
                })
                .show();
    }

    private void resetToOriginalImage() {


        if (originalBitmap != null) {
            imageView.setImageBitmap(originalBitmap);
            blurredBitmap = null;
            blurSlider.setProgress(0);  // Reset blur level
            Toast.makeText(this, "Image reset to original", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No original image loaded", Toast.LENGTH_SHORT).show();
        }


        // Launch OTP verification activity before resetting
        Intent intent = new Intent(MainActivity.this, LoginOTP.class);
        intent.putExtra("phone", "+254711188899"); // Replace with actual stored number
        startActivityForResult(intent, 101); // 101 = request code
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            boolean verified = data != null && data.getBooleanExtra("verified", false);
            if (verified) {
                // Perform the actual reset after verification
                imageView.setImageBitmap(originalBitmap);
                blurredBitmap = null;
                blurSlider.setProgress(0);
                Toast.makeText(this, "Image reset to original", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "OTP verification failed", Toast.LENGTH_SHORT).show();
            }
        }
    }




    private void requestCameraPermission() {
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
        }
    }

    private void startCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        someActivityResultLauncher.launch(cameraIntent);
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Bundle extras = data.getExtras();
                        if (extras != null && extras.containsKey("data")) {
                            originalBitmap = (Bitmap) extras.get("data");
                            imageView.setImageBitmap(originalBitmap);
                            applyBlur(imageView, originalBitmap, blurLevel);
                        }
                    }
                }
            }
    );

    ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    Glide.with(this)
                            .asBitmap()
                            .load(uri)
                            .into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    originalBitmap = resource;
                                    imageView.setImageBitmap(resource);
                                    applyBlur(imageView, originalBitmap, blurLevel);
                                }
                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {}
                            });
                }
            }
    );

    private void applyBlur(ImageView imageView, Bitmap bitmap, int level) {
        Glide.with(this)
                .asBitmap()
                .load(bitmap)
                .transform(new BlurTransformation(level, 8))
                .transition(BitmapTransitionOptions.withCrossFade())
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        blurredBitmap = resource;
                        imageView.setImageBitmap(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
    }

    private void saveImageToDevice() {
        SharedPreferences preferences = getSharedPreferences("userPrefs", MODE_PRIVATE);
        String savedPin = preferences.getString("userPin", null);

        if (savedPin == null) {
            Toast.makeText(this, "PIN not set. Cannot save image.", Toast.LENGTH_SHORT).show();
            return;
        }

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setHint("Enter your 4-digit PIN");

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Enter PIN")
                .setMessage("Please enter your PIN to save the image.")
                .setView(input)
                .setPositiveButton("OK", (dialog, which) -> {
                    String enteredPin = input.getText().toString();
                    if (enteredPin.equals(savedPin)) {
                        performSaveImage(); // only save if PIN matches
                    } else {
                        Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performSaveImage() {
        if (blurredBitmap != null) {
            try {
                File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String fileName = "blurred_image_" + timeStamp + ".png";
                File file = new File(directory, fileName);

                OutputStream outputStream = new FileOutputStream(file);
                blurredBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.flush();
                outputStream.close();

                Toast.makeText(this, "Blurred image saved to Downloads folder", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to save blurred image", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No blurred image available to save", Toast.LENGTH_SHORT).show();
        }
    }



}
