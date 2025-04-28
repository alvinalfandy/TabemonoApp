package com.example.aplikasichefai;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class ProfileRecipeAdapter extends RecyclerView.Adapter<ProfileRecipeAdapter.RecipeViewHolder> {

    private Context context;
    private List<Recipe> recipeList;
    private int layoutResource;
    private OnRecipeDeletedListener deleteListener;

    public interface OnRecipeDeletedListener {
        void onRecipeDeleted();
    }

    public ProfileRecipeAdapter(Context context, List<Recipe> recipeList, int layoutResource, OnRecipeDeletedListener listener) {
        this.context = context;
        this.recipeList = recipeList;
        this.layoutResource = layoutResource;
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(layoutResource, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);

        // Set recipe title
        holder.recipeTitle.setText(recipe.getTitle());

        // Set recipe description if available
        if (recipe.getDescription() != null && !recipe.getDescription().isEmpty()) {
            holder.recipeDescription.setText(recipe.getDescription());
        } else {
            holder.recipeDescription.setText("No description available");
        }

        // Set rating if available
        if (recipe.getAverageRating() > 0) {
            holder.ratingValue.setText(String.format("%.1f", recipe.getAverageRating()));
        } else {
            holder.ratingValue.setText("0.0");
        }

        // Load image if available
        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(recipe.getImageUrl())
                    .centerCrop()
                    .into(holder.recipeImage);
        } else {
            // Set default image if no image URL is available
            holder.recipeImage.setImageResource(R.drawable.default_food_image);
        }

        // Set click listener for the item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra("recipeId", recipe.getId());
                intent.putExtra("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                intent.putExtra("title", recipe.getTitle());
                intent.putExtra("imageUrl", recipe.getImageUrl());
                intent.putExtra("description", recipe.getDescription());
                intent.putStringArrayListExtra("ingredients", recipe.getIngredients());
                intent.putStringArrayListExtra("steps", recipe.getSteps());
                context.startActivity(intent);
            }
        });

        // Set options menu click listener
        holder.optionsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v, recipe);
            }
        });
    }

    private void showPopupMenu(View view, Recipe recipe) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.inflate(R.menu.recipe_options_menu);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_edit) {
                    // Handle edit action
                    Intent intent = new Intent(context, EditRecipeActivity.class);
                    intent.putExtra("recipeId", recipe.getId());
                    intent.putExtra("title", recipe.getTitle());
                    intent.putExtra("description", recipe.getDescription());
                    intent.putExtra("imageUrl", recipe.getImageUrl());
                    intent.putStringArrayListExtra("ingredients", recipe.getIngredients());
                    intent.putStringArrayListExtra("steps", recipe.getSteps());
                    context.startActivity(intent);
                    return true;
                } else if (item.getItemId() == R.id.action_delete) {
                    // Handle delete action
                    deleteRecipe(recipe);
                    return true;
                }
                return false;
            }
        });

        popupMenu.show();
    }

    private void deleteRecipe(Recipe recipe) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference recipeRef = FirebaseDatabase.getInstance()
                .getReference("user_recipes")
                .child(userId)
                .child(recipe.getId());

        recipeRef.removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Recipe deleted successfully", Toast.LENGTH_SHORT).show();
                        // Notify the activity that the recipe was deleted
                        if (deleteListener != null) {
                            deleteListener.onRecipeDeleted();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed to delete recipe: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView recipeImage, optionsMenu;
        TextView recipeTitle, recipeDescription, ratingValue;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.recipeImage);
            recipeTitle = itemView.findViewById(R.id.recipeTitle);
            recipeDescription = itemView.findViewById(R.id.recipeDescription);
            ratingValue = itemView.findViewById(R.id.ratingValue);
            optionsMenu = itemView.findViewById(R.id.recipeOptionsMenu);
        }
    }
}