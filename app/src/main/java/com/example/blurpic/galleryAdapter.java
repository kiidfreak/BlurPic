package com.example.blurpic;

import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class galleryAdapter extends RecyclerView.Adapter<galleryAdapter.ViewHolder> {

    private List<MainActivity.BlurredImage> blurredImages;

    // Constructor to initialize an empty list
    public galleryAdapter() {
        blurredImages = new ArrayList<>();
    }

    // Constructor to initialize with a provided list
    public galleryAdapter(List<MainActivity.BlurredImage> blurredImages) {
        this.blurredImages = blurredImages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MainActivity.BlurredImage blurredImage = blurredImages.get(position);

        if (blurredImage != null) {
            Bitmap blurredBitmap = blurredImage.getBlurredBitmap();

            if (blurredBitmap != null) {
                Log.d("galleryAdapter", "BlurredBitmap not null for position: " + position);

                // Convert the Bitmap to a byte array
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                blurredBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                holder.imageView.setOnClickListener(v -> {
                    // Launch your existing PIN verification activity
                    Intent pinVerificationIntent = new Intent(holder.itemView.getContext(), pinEntryGallery.class);

                    // Pass the byte array to the PIN verification activity
                    pinVerificationIntent.putExtra("IMAGE_BITMAP", byteArray);

                    Log.d("galleryAdapter", "Launching pinEntryGallery activity for position: " + position);
                    holder.itemView.getContext().startActivity(pinVerificationIntent);
                });

                holder.imageView.setImageBitmap(blurredImage.getBlurredBitmap());
                Log.d("galleryAdapter", "Set bitmap for gallery adapter ImageView: " + position);
            } else {
                // Handle the case where the blurred bitmap is null
                Log.e("galleryAdapter", "Blurred Bitmap is null for position: " + position);
            }
        } else {
            // Handle the case where the BlurredImage is null
            Log.e("galleryAdapter", "BlurredImage is null for position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return blurredImages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.galleryImageView);
        }
    }
}
