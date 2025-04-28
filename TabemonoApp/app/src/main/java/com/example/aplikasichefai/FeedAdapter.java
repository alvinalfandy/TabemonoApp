package com.example.aplikasichefai;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {

    private Context context;
    private List<FeedItem> feedList;
    private FirebaseAuth auth;

    public FeedAdapter(Context context, List<FeedItem> feedList) {
        this.context = context;
        this.feedList = feedList;
        this.auth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_feed, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        FeedItem feedItem = feedList.get(position);
        User user = feedItem.getUser();
        Recipe recipe = feedItem.getRecipe();

        // Get current user ID
        String currentUserId = auth.getCurrentUser().getUid();

        // Set user data
        holder.username.setText(user.getUsername());

        // Load user profile image
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(user.getProfileImageUrl())
                    .circleCrop()
                    .into(holder.userProfileImage);
        } else {
            // Default profile image
            holder.userProfileImage.setImageResource(R.drawable.ic_profile);
        }

        // Set recipe data
        holder.recipeTitle.setText(recipe.getTitle());
        holder.recipeDescription.setText(recipe.getDescription());

        // Load recipe image
        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(recipe.getImageUrl())
                    .centerCrop()
                    .into(holder.recipeImage);
        } else {
            // Default recipe image or placeholder
            holder.recipeImage.setImageResource(R.drawable.placeholder_image);
        }

        // Set click listener for the recipe card
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to recipe detail activity
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra("recipeId", recipe.getId());
                intent.putExtra("userId", user.getUserId());
                context.startActivity(intent);
            }
        });

        // Set click listener for username/profile to view user profile
        holder.userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if this is the current user's profile
                if (user.getUserId() != null && user.getUserId().equals(currentUserId)) {
                    // Navigate to ProfileActivity for current user
                    Intent intent = new Intent(context, ProfileActivity.class);
                    context.startActivity(intent);
                } else {
                    // Navigate to ViewUserProfileActivity for other users
                    Intent intent = new Intent(context, ViewUserProfileActivity.class);
                    intent.putExtra("userId", user.getUserId());
                    context.startActivity(intent);
                }
            }
        });

        // Also set the same click listener for username text
        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if this is the current user's profile
                if (user.getUserId() != null && user.getUserId().equals(currentUserId)) {
                    // Navigate to ProfileActivity for current user
                    Intent intent = new Intent(context, ProfileActivity.class);
                    context.startActivity(intent);
                } else {
                    // Navigate to ViewUserProfileActivity for other users
                    Intent intent = new Intent(context, ViewUserProfileActivity.class);
                    intent.putExtra("userId", user.getUserId());
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return feedList.size();
    }

    public static class FeedViewHolder extends RecyclerView.ViewHolder {
        ImageView userProfileImage, recipeImage;
        TextView username, recipeTitle, recipeDescription;

        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            userProfileImage = itemView.findViewById(R.id.userProfileImage);
            username = itemView.findViewById(R.id.username);
            recipeImage = itemView.findViewById(R.id.recipeImage);
            recipeTitle = itemView.findViewById(R.id.recipeTitle);
            recipeDescription = itemView.findViewById(R.id.recipeDescription);
        }
    }
}