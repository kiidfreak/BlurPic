package com.example.blurpic;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import jp.wasabeef.glide.transformations.BlurTransformation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Button camera, gallery, btnGallery;
    private ImageView imageView;
    private Bitmap unblurredBitmap;

    public static List<BlurredImage> blurredImageList = new ArrayList<>();
    private ActivityResultLauncher<String> galleryLauncher;
    private boolean isFirstImage = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camera = findViewById(R.id.button);
        gallery = findViewById(R.id.button2);
        imageView = findViewById(R.id.imageView);

        btnGallery = findViewById(R.id.btnGallery);
        btnGallery.setOnClickListener(view -> openGallery());

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageView.setImageURI(uri);
                        isFirstImage = true;
                    }
                }
        );

        gallery.setOnClickListener(view -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch("image/*");
        });

        camera.setOnClickListener(view -> requestCameraPermission());

        Button blurImageButton = findViewById(R.id.button3);
        blurImageButton.setOnClickListener(view -> blurImage(imageView, unblurredBitmap));
    }

    // In MainActivity.java
    private void openGallery() {
        Intent galleryIntent = new Intent(MainActivity.this, galleryActivity.class);

        // Create an ArrayList to hold BlurredImage objects
        ArrayList<BlurredImage> blurredImageArrayList = new ArrayList<>(blurredImageList);

        // Pass the list to GalleryActivity
        galleryIntent.putParcelableArrayListExtra("BLURRED_IMAGE_LIST", blurredImageArrayList);

        startActivity(galleryIntent);
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
                        if (data.hasExtra("data")) {
                            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                            unblurredBitmap = imageBitmap;
                            imageView.setImageBitmap(imageBitmap);
                            blurImage(imageView, unblurredBitmap);
                        } else {
                            Uri selectedImageUri = data.getData();
                            try {
                                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                                unblurredBitmap = imageBitmap;
                                imageView.setImageBitmap(imageBitmap);
                                blurImage(imageView, unblurredBitmap);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
    );

    private void applyBlur(ImageView imageView, Bitmap bitmap, Bitmap unblurredBitmap) {
        MultiTransformation<Bitmap> multiTransformation = new MultiTransformation<>(new BlurTransformation(25, 8));

        Glide.with(this)
                .asBitmap()
                .load(unblurredBitmap)
                .transform(multiTransformation)
                .transition(BitmapTransitionOptions.withCrossFade())
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        addBlurredImage(resource, unblurredBitmap);
                        imageView.setImageBitmap(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        Log.d(TAG, "onLoadCleared");
                    }
                });
    }

    public static class BlurredImage implements Parcelable {
        private Bitmap blurredBitmap;
        private Bitmap unblurredBitmap;

        public BlurredImage(Bitmap blurredBitmap, Bitmap unblurredBitmap) {
            this.blurredBitmap = blurredBitmap;
            this.unblurredBitmap = unblurredBitmap;
        }

        public Bitmap getBlurredBitmap() {
            return blurredBitmap;
        }

        public Bitmap getUnblurredBitmap() {
            return unblurredBitmap;
        }

        // Parcelable implementation (required for passing data between activities)
        protected BlurredImage(Parcel in) {
            blurredBitmap = in.readParcelable(Bitmap.class.getClassLoader());
            unblurredBitmap = in.readParcelable(Bitmap.class.getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(blurredBitmap, flags);
            dest.writeParcelable(unblurredBitmap, flags);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<BlurredImage> CREATOR = new Creator<BlurredImage>() {
            @Override
            public BlurredImage createFromParcel(Parcel in) {
                return new BlurredImage(in);
            }

            @Override
            public BlurredImage[] newArray(int size) {
                return new BlurredImage[size];
            }
        };
    }


    private void addBlurredImage(Bitmap blurredImage, Bitmap unblurredImage) {
        if (blurredImage != null && unblurredImage != null) {
            BlurredImage blurredImageItem = new BlurredImage(blurredImage, unblurredImage);
            blurredImageList.add(blurredImageItem);

            // Log to check if blurredImageItem is not null
            Log.d("MainActivity", "BlurredImageItem is not null: " + blurredImageItem);

            // After adding the images, open the ImageDetail activity
            openImageDetail(blurredImageItem);
        } else {
            Log.e("MainActivity", "Blurred Image Bitmap or Unblurred Image Bitmap is null");
        }
    }


// New method to open ImageDetail activity
private void openImageDetail(BlurredImage blurredImageItem) {
    Intent imageDetailIntent = new Intent(MainActivity.this, galleryActivity.class);

    // Pass the BlurredImage item using Parcelable
    imageDetailIntent.putExtra("BLURRED_IMAGE_ITEM", blurredImageItem);

    startActivity(imageDetailIntent);
}



    private void blurImage(ImageView imageView, Bitmap unblurredBitmap) {
        Drawable drawable = imageView.getDrawable();
        if (drawable != null && drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap originalBitmap = bitmapDrawable.getBitmap();

            if (originalBitmap != null) {
                applyBlur(imageView, originalBitmap, unblurredBitmap);
            } else {
                Toast.makeText(this, "Select a picture first", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Select a picture first", Toast.LENGTH_SHORT).show();
        }
    }
}
