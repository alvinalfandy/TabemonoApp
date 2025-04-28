package com.example.aplikasichefai;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private static final String TAG = "UserAdapter";

    private Context context;
    private List<User> userList;
    private String currentUserId;

    public UserAdapter(Context context, List<User> userList, String currentUserId) {
        this.context = context;
        this.userList = userList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);

        // Set user info
        holder.username.setText(user.getUsername());

        // Set name if available
        if (user.getName() != null && !user.getName().isEmpty()) {
            holder.name.setText(user.getName());
        } else {
            holder.name.setText("");
        }

        // Load profile image
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(user.getProfileImageUrl())
                    .circleCrop()
                    .into(holder.profileImage);
        } else {
            holder.profileImage.setImageResource(R.drawable.ic_profile);
        }

        // Don't show follow button for current user
        if (user.getUserId().equals(currentUserId)) {
            holder.followButton.setVisibility(View.GONE);
        } else {
            holder.followButton.setVisibility(View.VISIBLE);
            checkFollowingStatus(holder, user);
        }

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            // Navigate to user profile
            Intent intent = new Intent(context, ViewUserProfileActivity.class);
            intent.putExtra("userId", user.getUserId());
            context.startActivity(intent);
        });

        holder.followButton.setOnClickListener(v -> {
            // Toggle follow status
            if (holder.isFollowing) {
                unfollowUser(holder, user);
            } else {
                followUser(holder, user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    private void checkFollowingStatus(ViewHolder holder, User user) {
        DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference("following")
                .child(currentUserId).child(user.getUserId());

        followingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                holder.isFollowing = dataSnapshot.exists();
                updateFollowButton(holder);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error checking following status: " + databaseError.getMessage());
            }
        });
    }

    private void updateFollowButton(ViewHolder holder) {
        if (holder.isFollowing) {
            holder.followButton.setText("Following");
            holder.followButton.setBackgroundResource(R.drawable.button_secondary);
            holder.followButton.setTextColor(context.getResources().getColor(android.R.color.black));
        } else {
            holder.followButton.setText("Follow");
            holder.followButton.setBackgroundResource(R.drawable.button_primary);
            holder.followButton.setTextColor(context.getResources().getColor(android.R.color.white));
        }
    }

    private void followUser(ViewHolder holder, User user) {
        // Add to current user's following list
        DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference("following")
                .child(currentUserId).child(user.getUserId());
        followingRef.setValue(true);

        // Add to target user's followers list
        DatabaseReference followersRef = FirebaseDatabase.getInstance().getReference("followers")
                .child(user.getUserId()).child(currentUserId);
        followersRef.setValue(true);

        holder.isFollowing = true;
        updateFollowButton(holder);
        Toast.makeText(context, "Following " + user.getUsername(), Toast.LENGTH_SHORT).show();
    }

    private void unfollowUser(ViewHolder holder, User user) {
        // Remove from current user's following list
        DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference("following")
                .child(currentUserId).child(user.getUserId());
        followingRef.removeValue();

        // Remove from target user's followers list
        DatabaseReference followersRef = FirebaseDatabase.getInstance().getReference("followers")
                .child(user.getUserId()).child(currentUserId);
        followersRef.removeValue();

        holder.isFollowing = false;
        updateFollowButton(holder);
        Toast.makeText(context, "Unfollowed " + user.getUsername(), Toast.LENGTH_SHORT).show();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView username, name;
        Button followButton;
        boolean isFollowing;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            username = itemView.findViewById(R.id.username);
            name = itemView.findViewById(R.id.name);
            followButton = itemView.findViewById(R.id.followButton);
        }
    }
}