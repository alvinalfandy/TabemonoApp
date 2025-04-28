package com.example.aplikasichefai;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ViewUserProfileActivity extends AppCompatActivity {

    private ImageView profileImage, backButton;
    private TextView username, name, bio, recipesCount, followersCount, followingCount;
    private Button btnFollow, btnMessage;
    private RecyclerView recyclerView;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> userRecipesList;
    private FirebaseAuth auth;
    private String userId;
    private String currentUserId;
    private DatabaseReference userRecipesDatabaseReference;
    private ValueEventListener userRecipesListener;
    private boolean isFollowing = false;
    private static final String TAG = "ViewUserProfileActivity";

    // LinearLayouts for followers and following click targets
    private LinearLayout followersLayout, followingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user_profile);

        // Get userId from intent
        userId = getIntent().getStringExtra("userId");
        if (userId == null) {
            Toast.makeText(this, "User information not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        // Initialize UI
        initUI();

        // Setup click listeners
        setupClickListeners();

        // Load user data
        loadUserData();

        // Check if current user is following this user
        checkFollowingStatus();
    }

    private void initUI() {
        profileImage = findViewById(R.id.profileImage);
        username = findViewById(R.id.username);
        name = findViewById(R.id.name);
        bio = findViewById(R.id.bio);
        recipesCount = findViewById(R.id.recipesCount);
        followersCount = findViewById(R.id.followersCount);
        followingCount = findViewById(R.id.followingCount);
        btnFollow = findViewById(R.id.btnFollow);
        btnMessage = findViewById(R.id.btnMessage);
        recyclerView = findViewById(R.id.recyclerView);
        backButton = findViewById(R.id.backButton);

        // Get follow count layouts for click listeners
        followersLayout = findViewById(R.id.followersLayout);
        followingLayout = findViewById(R.id.followingLayout);

        // Initialize the recipes list
        userRecipesList = new ArrayList<>();

        // Setup RecyclerView with grid layout
        int spanCount = 2; // 2 columns
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, spanCount);
        recyclerView.setLayoutManager(gridLayoutManager);

        // Initialize adapter
        recipeAdapter = new RecipeAdapter(this, userRecipesList, R.layout.item_recipe);
        recyclerView.setAdapter(recipeAdapter);

        // Add GridSpacingItemDecoration for better spacing between items
        int spacing = 16; // 16dp spacing
        boolean includeEdge = true;
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, includeEdge));
    }

    private void loadUserData() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        username.setText(user.getUsername());
                        name.setText(user.getName());

                        if (user.getBio() != null && !user.getBio().isEmpty()) {
                            bio.setText(user.getBio());
                        } else {
                            bio.setText("No bio available");
                        }

                        // Load profile image if available
                        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                            Glide.with(ViewUserProfileActivity.this)
                                    .load(user.getProfileImageUrl())
                                    .circleCrop()
                                    .into(profileImage);
                        }

                        // Load user recipes
                        loadUserRecipes();

                        // Load followers and following counts
                        loadFollowersAndFollowing();
                    }
                } else {
                    Toast.makeText(ViewUserProfileActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading user data: " + databaseError.getMessage());
                Toast.makeText(ViewUserProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public String getUserId() {
        return userId;
    }

    private void loadUserRecipes() {
        // Clear recipes list before loading
        userRecipesList.clear();

        // Reference to database resep pengguna
        userRecipesDatabaseReference = FirebaseDatabase.getInstance().getReference("user_recipes").child(userId);

        userRecipesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userRecipesList.clear();
                int count = 0;

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    try {
                        Recipe recipe = dataSnapshot.getValue(Recipe.class);
                        if (recipe != null) {
                            // Ensure recipe ID is set
                            if (recipe.getId() == null || recipe.getId().isEmpty()) {
                                recipe.setId(dataSnapshot.getKey());
                            }

                            userRecipesList.add(recipe);
                            count++;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing recipe: " + e.getMessage());
                    }
                }

                // Update recipe count text
                recipesCount.setText(String.valueOf(count));

                // Notify adapter that data has changed
                recipeAdapter.notifyDataSetChanged();

                if (count == 0) {
                    Toast.makeText(ViewUserProfileActivity.this, "No recipes found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
                Toast.makeText(ViewUserProfileActivity.this, "Failed to load user recipes", Toast.LENGTH_SHORT).show();
            }
        };

        userRecipesDatabaseReference.addValueEventListener(userRecipesListener);
    }

    private void loadFollowersAndFollowing() {
        DatabaseReference followersRef = FirebaseDatabase.getInstance().getReference("followers").child(userId);
        followersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long count = dataSnapshot.getChildrenCount();
                followersCount.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading followers: " + databaseError.getMessage());
            }
        });

        DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference("following").child(userId);
        followingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long count = dataSnapshot.getChildrenCount();
                followingCount.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading following: " + databaseError.getMessage());
            }
        });
    }

    private void checkFollowingStatus() {
        DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference("following")
                .child(currentUserId).child(userId);

        followingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                isFollowing = dataSnapshot.exists();
                updateFollowButton();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error checking following status: " + databaseError.getMessage());
            }
        });
    }

    private void updateFollowButton() {
        if (isFollowing) {
            btnFollow.setText("Unfollow");
        } else {
            btnFollow.setText("Follow");
        }
    }

    private void setupClickListeners() {
        // Back button
        backButton.setOnClickListener(v -> finish());

        // Follow button
        btnFollow.setOnClickListener(v -> {
            if (userId.equals(currentUserId)) {
                Toast.makeText(ViewUserProfileActivity.this, "You cannot follow yourself", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isFollowing) {
                // Unfollow user
                unfollowUser();
            } else {
                // Follow user
                followUser();
            }
        });

        // Message button
        btnMessage.setOnClickListener(v -> {
            Intent intent = new Intent(ViewUserProfileActivity.this, ChatActivity.class);
            intent.putExtra("userId", userId);

            // Get the username from the TextView to pass to ChatActivity
            String usernameText = username.getText().toString();
            intent.putExtra("username", usernameText);

            startActivity(intent);
        });

        // Followers click listener
        followersLayout.setOnClickListener(v -> {
            Intent intent = new Intent(ViewUserProfileActivity.this, FollowListActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("listType", "followers");
            startActivity(intent);
        });

        // Following click listener
        followingLayout.setOnClickListener(v -> {
            Intent intent = new Intent(ViewUserProfileActivity.this, FollowListActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("listType", "following");
            startActivity(intent);
        });
    }

    private void followUser() {
        // Add to current user's following list
        DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference("following")
                .child(currentUserId).child(userId);
        followingRef.setValue(true);

        // Add to target user's followers list
        DatabaseReference followersRef = FirebaseDatabase.getInstance().getReference("followers")
                .child(userId).child(currentUserId);
        followersRef.setValue(true);

        isFollowing = true;
        updateFollowButton();
        loadFollowersAndFollowing();
        Toast.makeText(this, "Following user", Toast.LENGTH_SHORT).show();
    }

    private void unfollowUser() {
        // Remove from current user's following list
        DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference("following")
                .child(currentUserId).child(userId);
        followingRef.removeValue();

        // Remove from target user's followers list
        DatabaseReference followersRef = FirebaseDatabase.getInstance().getReference("followers")
                .child(userId).child(currentUserId);
        followersRef.removeValue();

        isFollowing = false;
        updateFollowButton();
        loadFollowersAndFollowing();
        Toast.makeText(this, "Unfollowed user", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove existing listeners to prevent memory leaks
        if (userRecipesDatabaseReference != null && userRecipesListener != null) {
            userRecipesDatabaseReference.removeEventListener(userRecipesListener);
        }
    }

    // GridSpacingItemDecoration class for better recipe grid layout
    public static class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(@NonNull android.graphics.Rect outRect, @NonNull View view,
                                   @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }
}