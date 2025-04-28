package com.example.aplikasichefai;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchUsersActivity extends AppCompatActivity {

    private static final String TAG = "SearchUsersActivity";

    private ImageView backButton;
    private EditText searchEditText;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView noResultsText;

    private SearchUserAdapter adapter;
    private List<User> userList;

    private FirebaseAuth auth;
    private DatabaseReference usersRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_users);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Initialize UI components
        initUI();

        // Setup recycler view
        setupRecyclerView();

        // Setup listeners
        setupListeners();
    }

    private void initUI() {
        backButton = findViewById(R.id.backButton);
        searchEditText = findViewById(R.id.searchEditText);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        noResultsText = findViewById(R.id.noResultsText);

        // Initially hide the progress bar and no results text
        progressBar.setVisibility(View.GONE);
        noResultsText.setVisibility(View.GONE);
    }

    private void setupRecyclerView() {
        userList = new ArrayList<>();
        adapter = new SearchUserAdapter(this, userList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        // Back button click listener
        backButton.setOnClickListener(v -> finish());

        // Search text change listener
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchText = s.toString().trim().toLowerCase();
                if (searchText.isEmpty()) {
                    userList.clear();
                    adapter.notifyDataSetChanged();
                    noResultsText.setVisibility(View.GONE);
                } else {
                    searchUsers(searchText);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });
    }

    private void searchUsers(String searchText) {
        progressBar.setVisibility(View.VISIBLE);
        noResultsText.setVisibility(View.GONE);

        // Clear the previous search results
        userList.clear();

        // Try two approaches:

        // 1. First try username_lower if it exists
        Query query = usersRef.orderByChild("username_lower")
                .startAt(searchText)
                .endAt(searchText + "\uf8ff")
                .limitToFirst(50);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                boolean foundResults = false;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null && !user.getUserId().equals(currentUserId)) {
                        // Ensure the user has an ID
                        if (user.getUserId() == null) {
                            user.setUserId(snapshot.getKey());
                        }
                        userList.add(user);
                        foundResults = true;
                    }
                }

                // If no results found with username_lower, try with username
                if (!foundResults) {
                    searchByUsername(searchText);
                } else {
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    noResultsText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                searchByUsername(searchText); // Try alternate approach
            }
        });
    }

    private void searchByUsername(String searchText) {
        // Try fallback approach: get all users and filter manually
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null && !user.getUserId().equals(currentUserId)) {
                        // Check if username contains the search text (case insensitive)
                        String username = user.getUsername() != null ? user.getUsername().toLowerCase() : "";
                        if (username.contains(searchText.toLowerCase())) {
                            if (user.getUserId() == null) {
                                user.setUserId(snapshot.getKey());
                            }
                            userList.add(user);
                        }
                    }
                }

                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

                // Show "No results" message if no users found
                if (userList.isEmpty()) {
                    noResultsText.setVisibility(View.VISIBLE);
                } else {
                    noResultsText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                progressBar.setVisibility(View.GONE);
                noResultsText.setVisibility(View.VISIBLE);
            }
        });
    }
}