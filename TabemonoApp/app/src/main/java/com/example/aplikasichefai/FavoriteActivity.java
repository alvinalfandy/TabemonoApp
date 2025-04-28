package com.example.aplikasichefai;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FavoriteActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecipeAdapter recipeAdapter;
    private FavoriteManager favoriteManager;
    private TextView emptyStateTextView;
    private ImageView homeIcon, favoriteIcon, aiIcon, addRecipeIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Halaman Favorite");
        }

        // Initialize views
        recyclerView = findViewById(R.id.favoriteRecyclerView);
        emptyStateTextView = findViewById(R.id.emptyStateTextView);

        // Initialize bottom navigation icons
        homeIcon = findViewById(R.id.homeIcon);
        favoriteIcon = findViewById(R.id.favoriteIcon);
        aiIcon = findViewById(R.id.aiIcon);
        addRecipeIcon = findViewById(R.id.addRecipeIcon);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Initialize FavoriteManager
        favoriteManager = new FavoriteManager(this);

        // Get favorite recipes
        List<Recipe> favoriteRecipes = favoriteManager.getFavorites();

        // Setup adapter
        recipeAdapter = new RecipeAdapter(this, favoriteRecipes);
        recyclerView.setAdapter(recipeAdapter);

        // Show/hide empty state
        updateEmptyState(favoriteRecipes);

        // Setup Bottom Navigation
        setupBottomNavigation();
    }

    // Setup Bottom Navigation
    private void setupBottomNavigation() {
        homeIcon.setOnClickListener(v -> {
            Intent intent = new Intent(FavoriteActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        favoriteIcon.setOnClickListener(v -> {
            // Current activity, do nothing
            Intent intent = new Intent(FavoriteActivity.this, FavoriteActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        aiIcon.setOnClickListener(v -> {
            // Placeholder for AI Chat functionality
            Intent intent = new Intent(FavoriteActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        addRecipeIcon.setOnClickListener(v -> {
            // Placeholder for AI Chat functionality
            Intent intent = new Intent(FavoriteActivity.this, AddRecipeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    // Update empty state visibility
    private void updateEmptyState(List<Recipe> recipes) {
        if (recipes == null || recipes.isEmpty()) {
            emptyStateTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh favorites when returning to the activity
        List<Recipe> favoriteRecipes = favoriteManager.getFavorites();
        recipeAdapter.setFilteredList(favoriteRecipes);
        updateEmptyState(favoriteRecipes);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle back button click
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}