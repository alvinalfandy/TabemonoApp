package com.example.aplikasichefai;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
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

public class FollowListActivity extends AppCompatActivity {

    private static final String TAG = "FollowListActivity";

    // UI components
    private ImageView backButton;
    private TextView titleText;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;

    // Adapter
    private UserAdapter userAdapter;
    private List<User> userList;

    // Firebase
    private FirebaseAuth auth;
    private String currentUserId;

    // Data
    private String userId;
    private String listType; // "followers" or "following"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_list);

        // Get data from intent
        userId = getIntent().getStringExtra("userId");
        listType = getIntent().getStringExtra("listType");

        if (userId == null || listType == null) {
            Toast.makeText(this, "Missing information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        // Initialize UI
        initUI();

        // Set title based on list type
        String title = listType.equals("followers") ? "Followers" : "Following";
        titleText.setText(title);

        // Setup RecyclerView
        setupRecyclerView();

        // Load users
        loadUsers();

        // Setup click listeners
        setupClickListeners();
    }

    private void initUI() {
        backButton = findViewById(R.id.backButton);
        titleText = findViewById(R.id.titleText);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyText = findViewById(R.id.emptyText);
    }

    private void setupRecyclerView() {
        userList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(this, userList, currentUserId);
        recyclerView.setAdapter(userAdapter);
    }

    private void loadUsers() {
        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyText.setVisibility(View.GONE);

        // Get database reference based on list type
        DatabaseReference reference;
        if (listType.equals("followers")) {
            reference = FirebaseDatabase.getInstance().getReference("followers").child(userId);
        } else {
            reference = FirebaseDatabase.getInstance().getReference("following").child(userId);
        }

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();

                // Check if there are any users
                if (!dataSnapshot.exists() || dataSnapshot.getChildrenCount() == 0) {
                    showEmptyState();
                    return;
                }

                // Count for tracking loaded users
                final int[] usersToLoad = {(int) dataSnapshot.getChildrenCount()};
                final int[] loadedUsers = {0};

                // Iterate through each follower or following
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String followUserId = snapshot.getKey();

                    // Get user details from users node
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(followUserId);
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            loadedUsers[0]++;

                            if (userSnapshot.exists()) {
                                User user = userSnapshot.getValue(User.class);
                                if (user != null) {
                                    // Ensure userId is set (needed for profile navigation)
                                    user.setUserId(followUserId);
                                    userList.add(user);
                                    userAdapter.notifyDataSetChanged();
                                }
                            }

                            // Check if all users are loaded
                            if (loadedUsers[0] >= usersToLoad[0]) {
                                progressBar.setVisibility(View.GONE);

                                if (userList.isEmpty()) {
                                    showEmptyState();
                                } else {
                                    recyclerView.setVisibility(View.VISIBLE);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            loadedUsers[0]++;
                            Log.e(TAG, "Error loading user: " + databaseError.getMessage());

                            // Check if all users are loaded
                            if (loadedUsers[0] >= usersToLoad[0]) {
                                progressBar.setVisibility(View.GONE);

                                if (userList.isEmpty()) {
                                    showEmptyState();
                                } else {
                                    recyclerView.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading users: " + databaseError.getMessage());
                Toast.makeText(FollowListActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    private void showEmptyState() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyText.setVisibility(View.VISIBLE);

        if (listType.equals("followers")) {
            emptyText.setText("No followers yet");
        } else {
            emptyText.setText("Not following anyone yet");
        }
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
    }
}