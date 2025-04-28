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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity implements ProfileRecipeAdapter.OnRecipeDeletedListener {

    private ImageView profileImage;
    private TextView username, name, bio, recipesCount, followersCount, followingCount;
    private Button btnFollow, btnMessage, btnFavorite;
    private RecyclerView recyclerView;
    private ProfileRecipeAdapter recipeAdapter; // Changed to ProfileRecipeAdapter
    private List<Recipe> userRecipesList;
    private FirebaseAuth auth;
    private DatabaseReference userRecipesDatabaseReference;
    private ValueEventListener userRecipesListener;
    private ImageView homeIcon, foodPageIcon, addRecipeIcon, aiIcon, profileIcon;
    private static final String TAG = "ProfileActivity";

    // Added LinearLayouts for followers and following click targets
    private LinearLayout followersLayout, followingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Check if user is logged in, if not redirect to login
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Initialize UI
        initUI();

        // Load user data
        loadUserData();

        // Setup click listeners
        setupClickListeners();

        // Load user recipes
        loadUserRecipes();
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
        btnFavorite = findViewById(R.id.btnFavorite);
        btnMessage = findViewById(R.id.btnMessage);
        recyclerView = findViewById(R.id.recyclerView);

        // Bottom navigation icons
        homeIcon = findViewById(R.id.homeIcon);
        foodPageIcon = findViewById(R.id.foodPageIcon);
        addRecipeIcon = findViewById(R.id.addRecipeIcon);
        aiIcon = findViewById(R.id.aiIcon);
        profileIcon = findViewById(R.id.profileIcon);

        // Get follow count layouts for click listeners
        followersLayout = findViewById(R.id.followersLayout);
        followingLayout = findViewById(R.id.followingLayout);

        // Initialize the recipes list
        userRecipesList = new ArrayList<>();

        // Setup RecyclerView with grid layout
        int spanCount = 2; // 2 columns
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, spanCount);
        recyclerView.setLayoutManager(gridLayoutManager);

        // Initialize adapter with our new custom adapter and layout
        recipeAdapter = new ProfileRecipeAdapter(this, userRecipesList, R.layout.item_recipe_profile, this);
        recyclerView.setAdapter(recipeAdapter);

        // Add GridSpacingItemDecoration for better spacing between items
        int spacing = 16; // 16dp spacing
        boolean includeEdge = true;
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, includeEdge));
    }

    // Grid Spacing Item Decoration class for better recipe grid layout
    private class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
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
                outRect.left = spacing - column * spacing / spanCount;
                outRect.right = (column + 1) * spacing / spanCount;
                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount;
                outRect.right = spacing - (column + 1) * spacing / spanCount;
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    private void loadUserData() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            username.setText(user.getUsername());

                            // Add this line to set the name/nickname
                            name.setText(user.getName());

                            if (user.getBio() != null && !user.getBio().isEmpty()) {
                                bio.setText(user.getBio());
                            } else {
                                bio.setText("No bio available");
                            }

                            // Load profile image if available
                            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                                Glide.with(ProfileActivity.this)
                                        .load(user.getProfileImageUrl())
                                        .circleCrop()
                                        .into(profileImage);
                            }

                            // Set button text for own profile
                            btnFollow.setText("Edit Profile");
                            btnMessage.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Error loading user data: " + databaseError.getMessage());
                    Toast.makeText(ProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadUserRecipes() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            // Clear recipes list before loading
            userRecipesList.clear();

            // Reference to database resep pengguna
            userRecipesDatabaseReference = FirebaseDatabase.getInstance().getReference("user_recipes").child(currentUser.getUid());

            // Show loading message
            Toast.makeText(this, "Loading recipes...", Toast.LENGTH_SHORT).show();

            userRecipesListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    userRecipesList.clear();
                    int count = 0;

                    Log.d(TAG, "Snapshot children count: " + snapshot.getChildrenCount());

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        try {
                            Recipe recipe = dataSnapshot.getValue(Recipe.class);
                            if (recipe != null) {
                                // Ensure recipe ID is set
                                if (recipe.getId() == null || recipe.getId().isEmpty()) {
                                    recipe.setId(dataSnapshot.getKey());
                                }

                                // Make sure all required fields are populated
                                if (recipe.getIngredients() == null) recipe.setIngredients(new ArrayList<>());
                                if (recipe.getSteps() == null) recipe.setSteps(new ArrayList<>());

                                // Debug log
                                Log.d(TAG, "Added recipe: " + recipe.getTitle() + " with ID: " + recipe.getId());

                                // Debug log for ratings
                                if (recipe.getRatings() != null) {
                                    Log.d(TAG, "Recipe ratings: " + recipe.getRatings().toString());
                                } else {
                                    Log.d(TAG, "No ratings for this recipe");
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

                    Log.d(TAG, "Loaded " + count + " user recipes");

                    if (count == 0) {
                        Toast.makeText(ProfileActivity.this, "No recipes found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Database error: " + error.getMessage());
                    Toast.makeText(ProfileActivity.this, "Failed to load user recipes", Toast.LENGTH_SHORT).show();
                }
            };

            userRecipesDatabaseReference.addValueEventListener(userRecipesListener);

            // Load followers and following counts
            loadFollowersAndFollowing(currentUser.getUid());
        }
    }

    private void loadFollowersAndFollowing(String userId) {
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

    private void setupClickListeners() {
        // Setup edit profile button
        btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch EditProfileActivity when Edit Profile is clicked
                Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                startActivity(intent);
            }
        });

        // Setup favorite button
        btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch FavoriteActivity when Favorite button is clicked
                Intent intent = new Intent(ProfileActivity.this, FavoriteActivity.class);
                startActivity(intent);
            }
        });

        // Setup bottom navigation icons click listeners
        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        foodPageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, FeedActivity.class);
                startActivity(intent);
            }
        });

        addRecipeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, AddRecipeActivity.class);
                startActivity(intent);
            }
        });

        aiIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Setup followers click listener
        followersLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentUserId = auth.getCurrentUser().getUid();
                Intent intent = new Intent(ProfileActivity.this, FollowListActivity.class);
                intent.putExtra("userId", currentUserId);
                intent.putExtra("listType", "followers");
                startActivity(intent);
            }
        });

        // Setup following click listener
        followingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentUserId = auth.getCurrentUser().getUid();
                Intent intent = new Intent(ProfileActivity.this, FollowListActivity.class);
                intent.putExtra("userId", currentUserId);
                intent.putExtra("listType", "following");
                startActivity(intent);
            }
        });
    }

    @Override
    public void onRecipeDeleted() {
        // Refresh recipe list after a recipe is deleted
        loadUserRecipes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        if (auth.getCurrentUser() != null) {
            loadUserRecipes();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove existing listeners to prevent memory leaks
        if (userRecipesDatabaseReference != null && userRecipesListener != null) {
            userRecipesDatabaseReference.removeEventListener(userRecipesListener);
        }
    }
}