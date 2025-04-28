package com.example.aplikasichefai;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private List<BannerItem> bannerItems;
    private Context context;

    public BannerAdapter(Context context, List<BannerItem> bannerItems) {
        this.context = context;
        this.bannerItems = bannerItems;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.banner_item, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        BannerItem currentItem = bannerItems.get(position);

        // Load image from URL using Glide
        Glide.with(context)
                .load(currentItem.getImageUrl())
                .placeholder(R.drawable.banner) // Use your default banner as placeholder
                .error(R.drawable.banner) // Use your default banner as error image
                .into(holder.bannerImageView);

        // Set click listener to open recipe details
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String recipeId = currentItem.getRecipeId();
                    Log.d("BannerAdapter", "Banner clicked with recipeId: " + recipeId);

                    // Instead of passing just the recipeId, let's load the recipe from Firebase
                    // and pass all the data needed for DetailActivity
                    Intent intent = new Intent(context, DetailActivity.class);
                    intent.putExtra("recipeId", recipeId);

                    // Add these fields to ensure DetailActivity has minimum required data
                    intent.putExtra("title", "Loading...");
                    intent.putExtra("description", "Loading recipe details...");
                    intent.putExtra("imageUrl", currentItem.getImageUrl());

                    context.startActivity(intent);
                } catch (Exception e) {
                    Log.e("BannerAdapter", "Error opening recipe details: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return bannerItems != null ? bannerItems.size() : 0;
    }

    public static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView bannerImageView;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            bannerImageView = itemView.findViewById(R.id.bannerImage);
        }
    }
}