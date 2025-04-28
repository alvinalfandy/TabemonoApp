package com.example.aplikasichefai;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DetailActivity extends AppCompatActivity {
    private ImageView recipeImage;
    private TextView titleText, descriptionText;
    private RecyclerView ingredientsRecycler, stepsRecycler;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private AppBarLayout appBarLayout;
    private NestedScrollView contentScrollView;
    private FloatingActionButton favoriteButton;
    private FavoriteManager favoriteManager;
    private Recipe currentRecipe;
    private ImageView aiIcon, homeIcon, addRecipeIcon, foodPageIcon, profileIcon;
    private static final String TAG = "DetailActivity";

    // Rating components
    private RatingBar ratingBar;
    private Button submitRatingButton;
    private TextView previousRatingText;
    private String recipeId;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Get current user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Initialize FavoriteManager
        favoriteManager = new FavoriteManager(this);

        // Initialize views
        recipeImage = findViewById(R.id.recipeImage);
        titleText = findViewById(R.id.titleText);
        descriptionText = findViewById(R.id.descriptionText);
        ingredientsRecycler = findViewById(R.id.ingredientsRecycler);
        stepsRecycler = findViewById(R.id.stepsRecycler);
        toolbar = findViewById(R.id.toolbar);
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        appBarLayout = findViewById(R.id.appBarLayout);
        contentScrollView = findViewById(R.id.contentScrollView);
        favoriteButton = findViewById(R.id.favoriteButton);

        // Initialize rating components
        ratingBar = findViewById(R.id.ratingBar);
        submitRatingButton = findViewById(R.id.submitRatingButton);
        previousRatingText = findViewById(R.id.previousRatingText);

        // Initialize bottom navigation icons
        profileIcon = findViewById(R.id.profileIcon);
        foodPageIcon = findViewById(R.id.foodPageIcon);
        homeIcon = findViewById(R.id.homeIcon);
        aiIcon = findViewById(R.id.aiIcon);
        addRecipeIcon = findViewById(R.id.addRecipeIcon);

        // Setup collapsing toolbar
        collapsingToolbar.setExpandedTitleColor(Color.TRANSPARENT);
        collapsingToolbar.setCollapsedTitleTextColor(Color.WHITE);

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(""); // Empty title since we'll display it in titleText
        }

        // Initialize RecyclerViews with empty lists to prevent null pointer exceptions
        setupRecyclerViews();

        // Get data from Intent
        Intent intent = getIntent();
        if (intent != null) {
            recipeId = intent.getStringExtra("recipeId");
            String imageUrl = intent.getStringExtra("imageUrl");
            String title = intent.getStringExtra("title");
            String description = intent.getStringExtra("description");
            ArrayList<String> ingredients = intent.getStringArrayListExtra("ingredients");
            ArrayList<String> steps = intent.getStringArrayListExtra("steps");

            // If we have a recipeId, load data from Firebase
            if (recipeId != null && !recipeId.isEmpty()) {
                loadRecipeFromFirebase(recipeId);
            } else {
                // If we don't have a recipeId but have a title, try to find the recipe by title
                if (title != null && !title.isEmpty()) {
                    findRecipeByTitle(title, imageUrl, description, ingredients, steps);
                } else {
                    // Otherwise use the data passed in the intent
                    setupRecipeData(title, imageUrl, description, ingredients, steps);
                }
            }
        }

        // Setup Bottom Navigation
        setupBottomNavigation();

        // Setup Rating Button
        setupRatingButton();
    }

    private void findRecipeByTitle(final String title, final String imageUrl, final String description,
                                   final ArrayList<String> ingredients, final ArrayList<String> steps) {
        if (title == null || title.isEmpty()) {
            setupRecipeData(title, imageUrl, description, ingredients, steps);
            return;
        }

        DatabaseReference recipesRef = FirebaseDatabase.getInstance().getReference("resep1");
        recipesRef.orderByChild("title").equalTo(title)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Recipe with this title exists, use the first match
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                recipeId = snapshot.getKey();
                                Log.d(TAG, "Found recipe by title with ID: " + recipeId);
                                loadRecipeFromFirebase(recipeId);
                                return;
                            }
                        } else {
                            // No recipe with this title found, use provided data
                            setupRecipeData(title, imageUrl, description, ingredients, steps);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Error finding recipe by title: " + databaseError.getMessage());
                        setupRecipeData(title, imageUrl, description, ingredients, steps);
                    }
                });
    }

    private void setupRatingButton() {
        submitRatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser == null) {
                    Toast.makeText(DetailActivity.this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (recipeId == null || recipeId.isEmpty()) {
                    // If we don't have a recipeId yet, try to find or save the recipe first
                    saveRecipeAndRating();
                } else {
                    // If we already have a recipeId, just save the rating
                    saveRating();
                }
            }
        });
    }

    private void saveRecipeAndRating() {
        // This method should only be called if we don't have a recipeId yet
        // We should check if the recipe already exists in Firebase based on title or other unique identifier

        if (currentRecipe == null) {
            Toast.makeText(DetailActivity.this, "Error: Recipe data is not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get userId from intent to determine if we're dealing with a user recipe
        String userId = getIntent().getStringExtra("userId");
        DatabaseReference recipesRef;

        if (userId != null && !userId.isEmpty()) {
            // Check in user_recipes
            recipesRef = FirebaseDatabase.getInstance().getReference("user_recipes").child(userId);
        } else {
            // Check in public recipes
            recipesRef = FirebaseDatabase.getInstance().getReference("resep1");
        }

        // First check if this recipe already exists by title
        recipesRef.orderByChild("title").equalTo(currentRecipe.getTitle())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Recipe with this title already exists, get its ID
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                recipeId = snapshot.getKey();
                                Log.d(TAG, "Found existing recipe with ID: " + recipeId);
                                // Now that we have the recipeId, save the rating
                                saveRating();
                                return;
                            }
                        } else {
                            // Recipe doesn't exist yet, create a new one
                            String newRecipeId = recipesRef.push().getKey();
                            if (newRecipeId != null) {
                                recipesRef.child(newRecipeId).setValue(currentRecipe)
                                        .addOnSuccessListener(aVoid -> {
                                            recipeId = newRecipeId;
                                            Log.d(TAG, "Created new recipe with ID: " + recipeId);
                                            saveRating();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(DetailActivity.this, "Error saving recipe: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Error checking for existing recipe: " + databaseError.getMessage());
                        Toast.makeText(DetailActivity.this, "Error checking for existing recipe", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveRating() {
        if (recipeId == null || recipeId.isEmpty() || currentUser == null) {
            Toast.makeText(this, "Unable to save rating", Toast.LENGTH_SHORT).show();
            return;
        }
        float rating = ratingBar.getRating();
        if (rating <= 0) {
            Toast.makeText(this, "Silakan pilih rating terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }
        // Get the user ID from intent, which will help us determine the recipe source
        String userId = getIntent().getStringExtra("userId");
        DatabaseReference recipeRef;
        // If userId is provided and it's not the current user, this is a user_recipe
        if (userId != null && !userId.isEmpty()) {
            recipeRef = FirebaseDatabase.getInstance().getReference("user_recipes").child(userId).child(recipeId);
        } else {
            // Otherwise check in the public recipes
            recipeRef = FirebaseDatabase.getInstance().getReference("resep1").child(recipeId);
        }
        // Get current recipe to update ratings
        recipeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Recipe recipe = dataSnapshot.getValue(Recipe.class);
                if (recipe != null) {
                    // Use the helper method from Recipe class to handle rating logic
                    recipe.addRating(currentUser.getUid(), rating);
                    // Update the recipe with new rating data
                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("ratings", recipe.getRatings());
                    updateData.put("averageRating", recipe.getAverageRating());
                    updateData.put("ratingCount", recipe.getRatingCount());
                    recipeRef.updateChildren(updateData)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(DetailActivity.this, "Rating berhasil disimpan!", Toast.LENGTH_SHORT).show();
                                // Update the button text
                                submitRatingButton.setText("Perbarui Rating");
                                // Update the previous rating text
                                if (previousRatingText != null) {
                                    previousRatingText.setVisibility(View.VISIBLE);
                                    previousRatingText.setText("Rating Anda sebelumnya: " + rating + " bintang");
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(DetailActivity.this, "Error saving rating: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading recipe: " + databaseError.getMessage());
                Toast.makeText(DetailActivity.this, "Error loading recipe data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerViews() {
        // Initialize with empty lists to prevent null pointer exceptions
        ArrayList<String> emptyList = new ArrayList<>();

        // Configure RecyclerView for ingredients
        ingredientsRecycler.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false; // Disable vertical scrolling in RecyclerView
            }
        });
        ingredientsRecycler.setNestedScrollingEnabled(false);
        ingredientsRecycler.setAdapter(new StringListAdapter(emptyList));

        // Configure RecyclerView for steps
        stepsRecycler.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false; // Disable vertical scrolling in RecyclerView
            }
        });
        stepsRecycler.setNestedScrollingEnabled(false);
        stepsRecycler.setAdapter(new StringListAdapter(emptyList));
    }

    private void loadRecipeFromFirebase(String recipeId) {
        Log.d(TAG, "Loading recipe with ID: " + recipeId);

        // Get userId from intent
        String userId = getIntent().getStringExtra("userId");

        if (userId != null && !userId.isEmpty()) {
            // First try to load from user_recipes if we have a userId
            DatabaseReference userRecipeRef = FirebaseDatabase.getInstance()
                    .getReference("user_recipes")
                    .child(userId)
                    .child(recipeId);

            userRecipeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Recipe recipe = dataSnapshot.getValue(Recipe.class);
                        if (recipe != null) {
                            Log.d(TAG, "Recipe found in user_recipes database: " + recipe.getTitle());
                            currentRecipe = recipe;
                            setupRecipeData(recipe.getTitle(), recipe.getImageUrl(),
                                    recipe.getDescription(), recipe.getIngredients(), recipe.getSteps());

                            // Check rating status
                            checkUserRating(recipe);
                        }
                    } else {
                        // If not found in user recipes, try public recipes
                        Log.d(TAG, "Recipe not found in user_recipes database, checking public recipes");
                        tryLoadFromPublicRecipes(recipeId);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Error loading recipe from user_recipes: " + databaseError.getMessage());
                    // Fallback to public recipes
                    tryLoadFromPublicRecipes(recipeId);
                }
            });
        } else {
            // If no userId provided, try public recipes directly
            tryLoadFromPublicRecipes(recipeId);
        }
    }

    private void tryLoadFromPublicRecipes(String recipeId) {
        // Try to load from public recipes
        DatabaseReference publicRef = FirebaseDatabase.getInstance().getReference("resep1").child(recipeId);
        publicRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Recipe recipe = dataSnapshot.getValue(Recipe.class);
                    if (recipe != null) {
                        Log.d(TAG, "Recipe found in public database: " + recipe.getTitle());
                        currentRecipe = recipe;
                        setupRecipeData(recipe.getTitle(), recipe.getImageUrl(),
                                recipe.getDescription(), recipe.getIngredients(), recipe.getSteps());

                        // Check rating status
                        checkUserRating(recipe);
                    }
                } else {
                    Log.d(TAG, "Recipe not found in any database");
                    Toast.makeText(DetailActivity.this, "Recipe not found or has been removed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading recipe: " + databaseError.getMessage());
                Toast.makeText(DetailActivity.this, "Error loading recipe details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserRating(Recipe recipe) {
        // Check if the current user has rated this recipe
        if (currentUser != null && recipe.getRatings() != null && recipe.getRatings().containsKey(currentUser.getUid())) {
            // Set the rating bar to the user's previous rating
            float userRating = recipe.getRatings().get(currentUser.getUid());
            ratingBar.setRating(userRating);
            // Update the button text to indicate this is an update
            submitRatingButton.setText("Perbarui Rating");
            // Show the user's previous rating
            if (previousRatingText != null) {
                previousRatingText.setVisibility(View.VISIBLE);
                previousRatingText.setText("Rating Anda sebelumnya: " + userRating + " bintang");
            }
        } else {
            submitRatingButton.setText("Kirim Rating");
            if (previousRatingText != null) {
                previousRatingText.setVisibility(View.GONE);
            }

            // Make sure rating components are visible
            ratingBar.setVisibility(View.VISIBLE);
            submitRatingButton.setVisibility(View.VISIBLE);
        }
    }

    private void setupRecipeData(String title, String imageUrl, String description, ArrayList<String> ingredients, ArrayList<String> steps) {
        // Make sure lists are not null to prevent crashes
        if (ingredients == null) ingredients = new ArrayList<>();
        if (steps == null) steps = new ArrayList<>();

        // Create Recipe object if it doesn't exist yet
        if (currentRecipe == null) {
            currentRecipe = new Recipe(title, imageUrl, description, ingredients, steps);
        }

        // Set data to UI
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).into(recipeImage);
        }

        // Set title in collapsing toolbar
        collapsingToolbar.setTitle(title);

        titleText.setText(title != null ? title : "");
        descriptionText.setText(description != null ? description : "");

        // Update RecyclerViews
        ingredientsRecycler.setAdapter(new StringListAdapter(ingredients));
        stepsRecycler.setAdapter(new StringListAdapter(steps));

        // Setup Favorite Button
        updateFavoriteButtonState();
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFavorite();
            }
        });
    }

    // Setup Bottom Navigation
    private void setupBottomNavigation() {
        homeIcon.setOnClickListener(v -> {
            Intent intent = new Intent(DetailActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        foodPageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailActivity.this, FeedActivity.class);
                startActivity(intent);
            }
        });

        aiIcon.setOnClickListener(v -> {
            Intent intent = new Intent(DetailActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        addRecipeIcon.setOnClickListener(v -> {
            Intent intent = new Intent(DetailActivity.this, AddRecipeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    // Toggle favorite status
    private void toggleFavorite() {
        if (favoriteManager.isFavorite(currentRecipe)) {
            favoriteManager.removeFromFavorites(currentRecipe);
            Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_SHORT).show();
        } else {
            favoriteManager.addToFavorites(currentRecipe);
            Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT).show();
        }
        updateFavoriteButtonState();
    }

    // Update favorite button state
    private void updateFavoriteButtonState() {
        if (favoriteManager.isFavorite(currentRecipe)) {
            favoriteButton.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            favoriteButton.setImageResource(R.drawable.ic_favorite_border);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle back button click
        if (item.getItemId() == android.R.id.home) {
            finish(); // Simply finish the current activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}