package com.example.aplikasichefai;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private static final String TAG = "RecipeAdapter";
    private Context context;
    private List<Recipe> recipeList;
    private Map<String, String> recipeIds; // Map of recipe titles to IDs
    private int layoutResId; // Resource ID for the layout to inflate

    public RecipeAdapter(Context context, List<Recipe> recipeList) {
        this.context = context;
        this.recipeList = recipeList;
        this.layoutResId = R.layout.item_recipe; // Default layout
    }

    public RecipeAdapter(Context context, List<Recipe> recipeList, int layoutResId) {
        this.context = context;
        this.recipeList = recipeList;
        this.layoutResId = layoutResId;
    }

    public RecipeAdapter(Context context, List<Recipe> recipeList, Map<String, String> recipeIds) {
        this.context = context;
        this.recipeList = recipeList;
        this.recipeIds = recipeIds;
        this.layoutResId = R.layout.item_recipe; // Default layout
    }

    public RecipeAdapter(Context context, List<Recipe> recipeList, Map<String, String> recipeIds, int layoutResId) {
        this.context = context;
        this.recipeList = recipeList;
        this.recipeIds = recipeIds;
        this.layoutResId = layoutResId;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(layoutResId, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);

        // Set title
        holder.title.setText(recipe.getTitle());

        // Check if the recipe has ratings
        Map<String, Float> ratings = recipe.getRatings();
        // Create final variables to use in lambda
        final float finalAvgRating;
        final int finalRatingCount;

        if (ratings != null && !ratings.isEmpty()) {
            // Calculate the average rating manually to ensure it's correct
            float sum = 0;
            for (Float rating : ratings.values()) {
                if (rating != null) {
                    sum += rating;
                }
            }
            finalRatingCount = ratings.size();
            finalAvgRating = finalRatingCount > 0 ? sum / finalRatingCount : 0;

            // Debug
            Log.d(TAG, "Recipe: " + recipe.getTitle() +
                    ", Sum: " + sum +
                    ", Count: " + finalRatingCount +
                    ", Avg: " + finalAvgRating);
        } else {
            finalAvgRating = 0;
            finalRatingCount = 0;
        }

        // Show the rating
        if (finalRatingCount > 0) {
            holder.ratingValue.setText(String.format("%.1f (%d)", finalAvgRating, finalRatingCount));
        } else {
            holder.ratingValue.setText("No ratings");
        }

        // Set description (showing a short preview)
        String description = recipe.getDescription();
        if (description != null && !description.isEmpty()) {
            holder.description.setText(description);
            holder.description.setVisibility(View.VISIBLE);
        } else {
            holder.description.setText("No description available");
            holder.description.setVisibility(View.VISIBLE);
        }

        // Load image using Glide with placeholder and error image
        Glide.with(context)
                .load(recipe.getImageUrl())
                .placeholder(R.drawable.placeholder_image) // Add a placeholder image resource
                .error(R.drawable.error_image) // Add an error image resource
                .into(holder.image);

        // Make sure ingredients and steps are not null before sending to Intent
        ArrayList<String> ingredients = recipe.getIngredients() != null ? new ArrayList<>(recipe.getIngredients()) : new ArrayList<>();
        ArrayList<String> steps = recipe.getSteps() != null ? new ArrayList<>(recipe.getSteps()) : new ArrayList<>();

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);

            // Add recipeId to intent
            if (recipe.getId() != null && !recipe.getId().isEmpty()) {
                intent.putExtra("recipeId", recipe.getId());
            } else if (recipeIds != null && recipeIds.containsKey(recipe.getTitle())) {
                intent.putExtra("recipeId", recipeIds.get(recipe.getTitle()));
            }

            // Pass the correct userId depending on which activity we're in
            if (context instanceof ProfileActivity) {
                // For profile, use current user ID
                intent.putExtra("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
            } else if (context instanceof ViewUserProfileActivity) {
                // For viewing another user's profile
                intent.putExtra("userId", ((ViewUserProfileActivity) context).getUserId());
            }

            // Get the recipeId for this recipe if available
            String recipeId = null;
            if (recipeIds != null && recipeIds.containsKey(recipe.getTitle())) {
                recipeId = recipeIds.get(recipe.getTitle());
                Log.d(TAG, "Found recipeId for " + recipe.getTitle() + ": " + recipeId);
            }

            // Add recipeId to intent if available
            if (recipeId != null && !recipeId.isEmpty()) {
                intent.putExtra("recipeId", recipeId);
            }

            // Also include all recipe data as fallback
            intent.putExtra("imageUrl", recipe.getImageUrl());
            intent.putExtra("title", recipe.getTitle());
            intent.putExtra("description", recipe.getDescription());
            intent.putExtra("ingredients", ingredients);
            intent.putExtra("steps", steps);

            // Pass rating information using the final variables
            intent.putExtra("averageRating", finalAvgRating);
            intent.putExtra("ratingCount", finalRatingCount);

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    // Method to update the list when filtering or new data is fetched from Firebase
    public void setFilteredList(List<Recipe> filteredList) {
        this.recipeList.clear();
        this.recipeList.addAll(filteredList);
        notifyDataSetChanged();
    }

    // Method to update recipe IDs map
    public void setRecipeIds(Map<String, String> recipeIds) {
        this.recipeIds = recipeIds;
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView description;
        ImageView image;
        TextView ratingValue;
        ImageView starIcon;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.recipeTitle);
            description = itemView.findViewById(R.id.recipeDescription);
            image = itemView.findViewById(R.id.recipeImage);
            ratingValue = itemView.findViewById(R.id.ratingValue);
            starIcon = itemView.findViewById(R.id.ratingStarIcon);
        }
    }
}