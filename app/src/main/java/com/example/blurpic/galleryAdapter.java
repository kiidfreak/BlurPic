package com.example.blurpic;

import android.content.Context;
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
import java.util.List;

public class galleryAdapter extends RecyclerView.Adapter<galleryAdapter.ViewHolder> {

    private Context context;
    private List<MainActivity.BlurredImage> blurredImageList;

    public galleryAdapter(Context context, List<MainActivity.BlurredImage> blurredImageList) {
        this.context = context;
        this.blurredImageList = blurredImageList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MainActivity.BlurredImage blurredImageItem = blurredImageList.get(position);

        if (blurredImageItem != null) {
            Bitmap blurredBitmap = blurredImageItem.getBlurredBitmap();

            if (blurredBitmap != null) {
                Log.d("galleryAdapter", "BlurredBitmap not null for position: " + position);

                // Convert the Bitmap to a byte array
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                blurredBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

// Modify the onItemClick method in your adapter to pass both blurred and unblurred bitmaps
                holder.imageView.setOnClickListener(v -> {
                    // Launch your existing PIN verification activity
                    Intent pinVerificationIntent = new Intent(holder.itemView.getContext(), pinEntryGallery.class);

                    // Pass the byte array to the PIN verification activity
                    pinVerificationIntent.putExtra("IMAGE_BITMAP", byteArray);

                    // Pass the unblurred bitmap as Parcelable
                    pinVerificationIntent.putExtra("UNBLURRED_IMAGE_BITMAP", blurredImageItem.getUnblurredBitmap());

                    Log.d("galleryAdapter", "Launching pinEntryGallery activity for position: " + position);
                    holder.itemView.getContext().startActivity(pinVerificationIntent);
                });



                holder.imageView.setImageBitmap(blurredBitmap);
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
        return blurredImageList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.galleryImageView);
        }
    }
}
