package com.example.aplikasichefai;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FeedActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FeedAdapter feedAdapter;
    private List<FeedItem> feedList;
    private FirebaseAuth auth;
    private DatabaseReference usersRef, recipesRef;
    private ImageView homeIcon, foodPageIcon, addRecipeIcon, aiIcon, profileIcon, messageIcon, searchIcon;
    private static final String TAG = "FeedActivity";
    private boolean isDataLoading = false; // Flag untuk mencegah multiple loading calls

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Check if user is logged in, if not redirect to login
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(FeedActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Initialize UI components
        initUI();

        // Setup click listeners for bottom navigation
        setupClickListeners();

        // Load feed data
        loadFeedData();
    }

    private void initUI() {
        recyclerView = findViewById(R.id.recyclerView);

        // Bottom navigation icons
        homeIcon = findViewById(R.id.homeIcon);
        foodPageIcon = findViewById(R.id.foodPageIcon);
        addRecipeIcon = findViewById(R.id.addRecipeIcon);
        aiIcon = findViewById(R.id.aiIcon);
        profileIcon = findViewById(R.id.profileIcon);

        // Initialize message icon
        messageIcon = findViewById(R.id.messageIcon);

        // Initialize search icon
        searchIcon = findViewById(R.id.searchIcon);

        // Initialize feed list
        feedList = new ArrayList<>();

        // Setup RecyclerView with linear layout
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Initialize adapter
        feedAdapter = new FeedAdapter(this, feedList);
        recyclerView.setAdapter(feedAdapter);
    }

    private void setupClickListeners() {
        // Setup bottom navigation icons click listeners
        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FeedActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        foodPageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FeedActivity.this, FeedActivity.class);
                startActivity(intent);
            }
        });

        addRecipeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FeedActivity.this, AddRecipeActivity.class);
                startActivity(intent);
            }
        });

        aiIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FeedActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FeedActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Setup message icon click listener
        messageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FeedActivity.this, MessageListActivity.class);
                startActivity(intent);
            }
        });

        // Setup search icon click listener
        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FeedActivity.this, SearchUsersActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadFeedData() {
        // Cek apakah proses loading sedang berlangsung
        if (isDataLoading) {
            return;
        }

        isDataLoading = true;

        // Bersihkan data yang ada sebelum memuat yang baru
        feedList.clear();
        feedAdapter.notifyDataSetChanged();

        // Show loading message
        Toast.makeText(this, "Loading feed...", Toast.LENGTH_SHORT).show();

        // Get references to Firebase database
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // First, get all users
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Counter untuk melacak jumlah user yang telah diproses
                final int[] userCount = {0};
                final int totalUsers = (int) dataSnapshot.getChildrenCount();

                if (totalUsers == 0) {
                    isDataLoading = false;
                    return;
                }

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    User user = userSnapshot.getValue(User.class);

                    // Only proceed if we got valid user data
                    if (user != null) {
                        // Load recipes for this user
                        loadUserRecipes(userId, user, new OnRecipesLoadedListener() {
                            @Override
                            public void onRecipesLoaded() {
                                userCount[0]++;
                                // Jika semua user telah diproses, reset flag loading
                                if (userCount[0] >= totalUsers) {
                                    isDataLoading = false;
                                }
                            }
                        });
                    } else {
                        userCount[0]++;
                        if (userCount[0] >= totalUsers) {
                            isDataLoading = false;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading users: " + databaseError.getMessage());
                Toast.makeText(FeedActivity.this, "Failed to load feed data", Toast.LENGTH_SHORT).show();
                isDataLoading = false;
            }
        });
    }

    // Interface untuk melacak kapan loading resep selesai
    interface OnRecipesLoadedListener {
        void onRecipesLoaded();
    }

    private void loadUserRecipes(String userId, User user, OnRecipesLoadedListener listener) {
        DatabaseReference userRecipesRef = FirebaseDatabase.getInstance().getReference("user_recipes").child(userId);

        userRecipesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
                    try {
                        Recipe recipe = recipeSnapshot.getValue(Recipe.class);
                        if (recipe != null) {
                            // Ensure recipe ID is set
                            if (recipe.getId() == null || recipe.getId().isEmpty()) {
                                recipe.setId(recipeSnapshot.getKey());
                            }

                            // Create a FeedItem with user data and recipe
                            FeedItem feedItem = new FeedItem(user, recipe);
                            feedList.add(feedItem);

                            // Debug
                            Log.d(TAG, "Added recipe from user " + user.getUsername() + ": " + recipe.getTitle());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing recipe: " + e.getMessage());
                    }
                }

                // Notify adapter after adding recipes for this user
                feedAdapter.notifyDataSetChanged();

                // Notify that recipes for this user have been loaded
                listener.onRecipesLoaded();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading recipes for user " + userId + ": " + databaseError.getMessage());
                listener.onRecipesLoaded(); // Tetap perlu memberi tahu bahwa proses selesai
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Selalu refresh data saat aktivitas dilanjutkan, tapi hanya jika tidak sedang loading
        if (auth.getCurrentUser() != null && !isDataLoading) {
            loadFeedData();
        }
    }
}