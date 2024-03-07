package com.example.blurpic;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class galleryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private galleryAdapter galleryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        recyclerView = findViewById(R.id.recyclerView);
        galleryAdapter = new galleryAdapter(getBlurredImages());
        recyclerView.setAdapter(galleryAdapter);
        // Set the GridLayoutManager
        int spanCount = 3; // You can adjust the span count as needed
        recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
    }

    private List<MainActivity.BlurredImage> getBlurredImages() {
        // Return the list of blurred images from the in-memory list
        return MainActivity.blurredImageList;
    }
}
