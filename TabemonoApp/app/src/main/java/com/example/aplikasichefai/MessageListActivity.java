package com.example.aplikasichefai;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MessageListActivity extends AppCompatActivity {

    private static final String TAG = "MessageListActivity";

    // UI Components
    private ImageView backButton;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;

    // Adapter
    private ConversationAdapter conversationAdapter;
    private List<Conversation> conversationList;

    // Firebase
    private FirebaseAuth auth;
    private String currentUserId;
    private DatabaseReference conversationsRef;
    private ValueEventListener conversationsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        currentUserId = auth.getCurrentUser().getUid();

        // Initialize UI
        initUI();

        // Setup RecyclerView
        setupRecyclerView();

        // Load conversations
        loadConversations();

        // Setup click listeners
        setupClickListeners();
    }

    private void initUI() {
        backButton = findViewById(R.id.backButton);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyText = findViewById(R.id.emptyText);
    }

    private void setupRecyclerView() {
        conversationList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        conversationAdapter = new ConversationAdapter(this, conversationList);
        recyclerView.setAdapter(conversationAdapter);
    }

    private void loadConversations() {
        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyText.setVisibility(View.GONE);

        // Get conversations reference
        conversationsRef = FirebaseDatabase.getInstance().getReference("conversations").child(currentUserId);

        conversationsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                conversationList.clear();

                if (!dataSnapshot.exists() || dataSnapshot.getChildrenCount() == 0) {
                    showEmptyState();
                    return;
                }

                // Track loaded conversations
                final int[] conversationsToLoad = {(int) dataSnapshot.getChildrenCount()};
                final int[] loadedConversations = {0};

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Conversation conversation = snapshot.getValue(Conversation.class);
                    if (conversation != null) {
                        // Ensure conversation ID is set
                        conversation.setId(snapshot.getKey());

                        // Load user data for this conversation
                        loadUserForConversation(conversation, loadedConversations, conversationsToLoad);
                    } else {
                        loadedConversations[0]++;
                        // Check if all conversations are loaded
                        if (loadedConversations[0] >= conversationsToLoad[0]) {
                            updateConversationsList();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading conversations: " + databaseError.getMessage());
                showEmptyState();
            }
        };

        conversationsRef.addValueEventListener(conversationsListener);
    }

    private void loadUserForConversation(Conversation conversation, final int[] loadedConversations, final int[] conversationsToLoad) {
        // Check if userId is null
        if (conversation.getUserId() == null) {
            Log.w(TAG, "Found conversation with null userId: " + conversation.getId());
            loadedConversations[0]++;

            // Check if all conversations are loaded
            if (loadedConversations[0] >= conversationsToLoad[0]) {
                updateConversationsList();
            }
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(conversation.getUserId());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                loadedConversations[0]++;

                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        // Ensure userId is set
                        user.setUserId(conversation.getUserId());
                        conversation.setUser(user);
                        conversationList.add(conversation);
                    }
                }

                // Check if all conversations are loaded
                if (loadedConversations[0] >= conversationsToLoad[0]) {
                    progressBar.setVisibility(View.GONE);

                    if (conversationList.isEmpty()) {
                        showEmptyState();
                    } else {
                        // Sort conversations by timestamp (newest first)
                        Collections.sort(conversationList, new Comparator<Conversation>() {
                            @Override
                            public int compare(Conversation c1, Conversation c2) {
                                return Long.compare(c2.getTimestamp(), c1.getTimestamp());
                            }
                        });

                        recyclerView.setVisibility(View.VISIBLE);
                        conversationAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                loadedConversations[0]++;
                Log.e(TAG, "Error loading user for conversation: " + databaseError.getMessage());

                // Check if all conversations are loaded
                if (loadedConversations[0] >= conversationsToLoad[0]) {
                    updateConversationsList();
                }
            }
        });
    }

    private void updateConversationsList() {
        progressBar.setVisibility(View.GONE);

        if (conversationList.isEmpty()) {
            showEmptyState();
        } else {
            // Sort conversations by timestamp (newest first)
            Collections.sort(conversationList, new Comparator<Conversation>() {
                @Override
                public int compare(Conversation c1, Conversation c2) {
                    return Long.compare(c2.getTimestamp(), c1.getTimestamp());
                }
            });

            recyclerView.setVisibility(View.VISIBLE);
            conversationAdapter.notifyDataSetChanged();
        }
    }

    private void showEmptyState() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyText.setVisibility(View.VISIBLE);
        emptyText.setText("No messages yet");
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove database listener to prevent memory leaks
        if (conversationsRef != null && conversationsListener != null) {
            conversationsRef.removeEventListener(conversationsListener);
        }
    }
}