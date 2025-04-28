package com.example.aplikasichefai;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recommendedRecipesRecyclerView;
    private RecyclerView popularRecipesRecyclerView;
    private RecipeAdapter recommendedRecipeAdapter;
    private RecipeAdapter popularRecipeAdapter;
    private List<Recipe> allRecipesList;
    private List<Recipe> recommendedRecipesList;
    private List<Recipe> popularRecipesList;
    private DatabaseReference publicDatabaseReference;
    private DatabaseReference userDatabaseReference;
    private ValueEventListener publicRecipesListener;
    private ValueEventListener userRecipesListener;
    private SearchView searchView;
    private ImageView aiIcon, homeIcon, addRecipeIcon, foodPageIcon, profileIcon;
    private TextView userGreeting;
    private Button btnLogout;
    private FirebaseAuth auth;

    // Banner carousel components
    private ViewPager2 bannerViewPager;
    private TabLayout bannerIndicator;
    private BannerAdapter bannerAdapter;
    private Handler sliderHandler = new Handler();
    private int currentPage = 0;
    private static final long SLIDER_DELAY = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Check if user is logged in, if not redirect to login
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Initialize UI
        initUI();

        // Setup Banner Carousel
        setupBannerCarousel();

        // Load user name from database
        loadUserName();

        // Load recipes from Firebase
        loadRecipes();

        // Setup click listeners
        setupClickListeners();

        // Setup app bar scrolling behavior if needed
        setupAppBarBehavior();
    }

    private void setupAppBarBehavior() {
        // You can add custom behavior here if needed
        // For example, custom animations or state change listeners

        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                // You can respond to scroll events here if needed
                // For example, changing search view appearance based on scroll position

                // This is optional - the layout will handle the sticky behavior automatically
                // But you can add custom behavior like changing search view text color, etc.
            }
        });
    }

    private void setupBannerCarousel() {
        // Initialize banner items list
        List<BannerItem> bannerItems = new ArrayList<>();

        // Setup ViewPager with empty adapter first
        bannerAdapter = new BannerAdapter(this, bannerItems);
        bannerViewPager.setAdapter(bannerAdapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(bannerIndicator, bannerViewPager,
                (tab, position) -> {
                    // No text for this tab
                }
        ).attach();

        // Load banners from Firebase
        DatabaseReference bannersRef = FirebaseDatabase.getInstance().getReference("banners");
        bannersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<BannerItem> newBannerItems = new ArrayList<>();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    BannerItem bannerItem = dataSnapshot.getValue(BannerItem.class);
                    if (bannerItem != null) {
                        newBannerItems.add(bannerItem);
                    }
                }

                // Update adapter with new data
                bannerAdapter = new BannerAdapter(HomeActivity.this, newBannerItems);
                bannerViewPager.setAdapter(bannerAdapter);

                // Reset auto sliding
                currentPage = 0;
                sliderHandler.removeCallbacks(sliderRunnable);

                // Only start auto-sliding if we have banners
                if (!newBannerItems.isEmpty()) {
                    sliderHandler.postDelayed(sliderRunnable, SLIDER_DELAY);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Failed to load banners", Toast.LENGTH_SHORT).show();
            }
        });

        // Auto sliding functionality
        bannerViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPage = position;
                // Reset timer when user manually changes page
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, SLIDER_DELAY);
            }
        });
    }

    private final Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (bannerViewPager == null || bannerAdapter == null || bannerAdapter.getItemCount() == 0) return;

            if (currentPage == bannerAdapter.getItemCount() - 1) {
                currentPage = 0;
            } else {
                currentPage++;
            }

            bannerViewPager.setCurrentItem(currentPage, true);
            sliderHandler.postDelayed(this, SLIDER_DELAY);
        }
    };

    private void initUI() {
        recommendedRecipesRecyclerView = findViewById(R.id.recommendedRecipesRecyclerView);
        popularRecipesRecyclerView = findViewById(R.id.popularRecipesRecyclerView);
        searchView = findViewById(R.id.searchView);
        aiIcon = findViewById(R.id.aiIcon);
        foodPageIcon = findViewById(R.id.foodPageIcon);
        homeIcon = findViewById(R.id.homeIcon);
        profileIcon = findViewById(R.id.profileIcon);
        addRecipeIcon = findViewById(R.id.addRecipeIcon);
        userGreeting = findViewById(R.id.userGreeting);
        btnLogout = findViewById(R.id.btnLogout);

        // Banner components
        bannerViewPager = findViewById(R.id.bannerViewPager);
        bannerIndicator = findViewById(R.id.bannerIndicator);

        // Setup recycler views
        recommendedRecipesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recommendedRecipesRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view);
                if (position > 0) {
                    outRect.left = 12;
                }
            }
        });

        // For the popular recipes grid layout
        int spanCount = 2; // 2 columns
        int spacing = 16; // spacing between items in dp
        boolean includeEdge = true;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, spanCount);
        popularRecipesRecyclerView.setLayoutManager(gridLayoutManager);
        popularRecipesRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, includeEdge));

        // Initialize lists
        allRecipesList = new ArrayList<>();
        recommendedRecipesList = new ArrayList<>();
        popularRecipesList = new ArrayList<>();

        // Setup adapters
        recommendedRecipeAdapter = new RecipeAdapter(this, new ArrayList<>(), R.layout.item_recommendation_recipe);
        popularRecipeAdapter = new RecipeAdapter(this, new ArrayList<>(), R.layout.item_recipe);

        // Set adapters to recycler views
        recommendedRecipesRecyclerView.setAdapter(recommendedRecipeAdapter);
        popularRecipesRecyclerView.setAdapter(popularRecipeAdapter);

        // Setup search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });
    }

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
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
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

    private void setupClickListeners() {
        // Setup logout button
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Log out the user
                auth.signOut();
                Toast.makeText(HomeActivity.this, "Logout successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                finish();
            }
        });

        // Setup bottom navigation icons click listeners
        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // We're already on the Home screen, so refresh data
                loadRecipes();
            }
        });

        foodPageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, FeedActivity.class);
                startActivity(intent);
            }
        });

        addRecipeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, AddRecipeActivity.class);
                startActivity(intent);
            }
        });

        aiIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start auto sliding when activity is in foreground
        if (bannerAdapter != null && bannerAdapter.getItemCount() > 0) {
            sliderHandler.postDelayed(sliderRunnable, SLIDER_DELAY);
        }

        // Refresh recipes when returning to home screen
        if (auth.getCurrentUser() != null) {
            loadRecipes();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop auto sliding when activity is not in foreground
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_my_recipes) {
            Intent intent = new Intent(HomeActivity.this, MyRecipesActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_add_recipe) {
            Intent intent = new Intent(HomeActivity.this, AddRecipeActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_logout) {
            auth.signOut();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Load all recipes from Firebase
    private void loadRecipes() {
        // Clear lists before loading new data
        allRecipesList.clear();
        recommendedRecipesList.clear();
        popularRecipesList.clear();

        // Update adapters with empty lists
        recommendedRecipeAdapter.setFilteredList(new ArrayList<>());
        popularRecipeAdapter.setFilteredList(new ArrayList<>());

        // Remove existing listeners
        removeListeners();

        // Load public recipes first
        publicDatabaseReference = FirebaseDatabase.getInstance().getReference("resep1");
        publicRecipesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("FirebaseData", "Public recipes loaded");
                allRecipesList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Recipe recipe = dataSnapshot.getValue(Recipe.class);
                    if (recipe != null) {
                        allRecipesList.add(recipe);
                    }
                }

                // After loading public recipes, load user recipes
                loadUserRecipes();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Failed to load public recipes", Toast.LENGTH_SHORT).show();
                // Still try to load user recipes even if public ones fail
                loadUserRecipes();
            }
        };
        publicDatabaseReference.addValueEventListener(publicRecipesListener);
    }

    // Load user's own recipes - UPDATED TO FIX DUPLICATE RECIPES ISSUE
    private void loadUserRecipes() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            userDatabaseReference = FirebaseDatabase.getInstance().getReference("marsha").child(currentUser.getUid());
            userRecipesListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.d("FirebaseData", "User recipes loaded");
                    List<Recipe> userRecipes = new ArrayList<>();

                    // Create a Set to keep track of recipes we've already included
                    Set<String> includedRecipeTitles = new HashSet<>();

                    // First add all existing recipes' titles to the set
                    for (Recipe recipe : allRecipesList) {
                        includedRecipeTitles.add(recipe.getTitle());
                    }

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Recipe recipe = dataSnapshot.getValue(Recipe.class);
                        if (recipe != null && !includedRecipeTitles.contains(recipe.getTitle())) {
                            // Only add this recipe if we haven't already included one with the same title
                            userRecipes.add(recipe);
                            includedRecipeTitles.add(recipe.getTitle());
                        }
                    }

                    // Add user recipes to the main list
                    allRecipesList.addAll(userRecipes);

                    // Split recipes into recommended and popular lists
                    distributeRecipes();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(HomeActivity.this, "Failed to load user recipes", Toast.LENGTH_SHORT).show();
                    // Still distribute whatever recipes we have
                    distributeRecipes();
                }
            };
            userDatabaseReference.addValueEventListener(userRecipesListener);
        } else {
            // Just distribute public recipes if no user is logged in
            distributeRecipes();
        }
    }

    // Split recipes between recommended and popular sections
    private void distributeRecipes() {
        recommendedRecipesList.clear();
        popularRecipesList.clear();

        if (allRecipesList.size() > 0) {
            // First half goes to recommended
            int halfSize = allRecipesList.size() / 2;
            for (int i = 0; i < halfSize && i < allRecipesList.size(); i++) {
                recommendedRecipesList.add(allRecipesList.get(i));
            }

            // Second half goes to popular
            for (int i = halfSize; i < allRecipesList.size(); i++) {
                popularRecipesList.add(allRecipesList.get(i));
            }
        }

        // Update the adapters
        recommendedRecipeAdapter.setFilteredList(recommendedRecipesList);
        popularRecipeAdapter.setFilteredList(popularRecipesList);

        Log.d("FirebaseData", "Total recipes: " + allRecipesList.size() +
                ", Recommended: " + recommendedRecipesList.size() +
                ", Popular: " + popularRecipesList.size());
    }

    // Load user name from Firebase
    private void loadUserName() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            userGreeting.setText("Hi, " + user.getName());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("HomeActivity", "Error loading user data: " + databaseError.getMessage());
                }
            });
        }
    }

    // Filter recipes based on search query
    private void filterList(String query) {
        if (query.isEmpty()) {
            // Reset to original distribution if search query is empty
            distributeRecipes();
            return;
        }

        List<Recipe> filteredRecommended = new ArrayList<>();
        List<Recipe> filteredPopular = new ArrayList<>();

        // Filter recommended recipes
        for (Recipe recipe : recommendedRecipesList) {
            if (recipe.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredRecommended.add(recipe);
            }
        }

        // Filter popular recipes
        for (Recipe recipe : popularRecipesList) {
            if (recipe.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredPopular.add(recipe);
            }
        }

        // Update adapters with filtered lists
        recommendedRecipeAdapter.setFilteredList(filteredRecommended);
        popularRecipeAdapter.setFilteredList(filteredPopular);
    }

    private void removeListeners() {
        // Remove existing listeners to prevent memory leaks
        if (publicDatabaseReference != null && publicRecipesListener != null) {
            publicDatabaseReference.removeEventListener(publicRecipesListener);
        }

        if (userDatabaseReference != null && userRecipesListener != null) {
            userDatabaseReference.removeEventListener(userRecipesListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeListeners();
        // Make sure to remove callbacks to prevent memory leaks
        sliderHandler.removeCallbacks(sliderRunnable);
    }
}