package com.example.aplikasichefai;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    // UI Components
    private ImageView backButton;
    private ImageView profileImage;
    private TextView usernameText;
    private RecyclerView recyclerView;
    private EditText messageInput;
    private ImageView sendButton;
    private ProgressBar progressBar;

    // Message Adapter
    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    // Firebase
    private FirebaseAuth auth;
    private String currentUserId;
    private DatabaseReference messagesRef;
    private ValueEventListener messagesListener;

    // User data
    private String otherUserId;
    private String otherUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Get data from intent
        otherUserId = getIntent().getStringExtra("userId");
        otherUsername = getIntent().getStringExtra("username");

        if (otherUserId == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            finish();
            return;
        }
        currentUserId = auth.getCurrentUser().getUid();

        // Initialize UI
        initUI();

        // Setup user info
        usernameText.setText(otherUsername != null ? otherUsername : "User");
        loadUserProfileImage();

        // Setup RecyclerView
        setupRecyclerView();

        // Load messages
        loadMessages();

        // Mark conversation as read
        markConversationAsRead();

        // Setup click listeners
        setupClickListeners();
    }

    private void initUI() {
        backButton = findViewById(R.id.backButton);
        profileImage = findViewById(R.id.profileImage);
        usernameText = findViewById(R.id.username);
        recyclerView = findViewById(R.id.recyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void loadUserProfileImage() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(otherUserId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null && user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                        Glide.with(ChatActivity.this)
                                .load(user.getProfileImageUrl())
                                .circleCrop()
                                .into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading user profile image: " + databaseError.getMessage());
            }
        });
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Scroll to bottom on new messages
        recyclerView.setLayoutManager(layoutManager);

        // Get chat ID for both users
        String chatId = getChatId(currentUserId, otherUserId);

        // Pass chatId to the adapter
        messageAdapter = new MessageAdapter(this, messageList, currentUserId, chatId);
        recyclerView.setAdapter(messageAdapter);
    }

    private void loadMessages() {
        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);

        // Use a chat ID that is the same for both users
        String chatId = getChatId(currentUserId, otherUserId);

        // Get messages reference
        messagesRef = FirebaseDatabase.getInstance().getReference("messages").child(chatId);

        messagesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    if (message != null) {
                        // Ensure message ID is set
                        message.setId(snapshot.getKey());
                        messageList.add(message);
                    }
                }

                messageAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

                // Scroll to bottom if there are messages
                if (!messageList.isEmpty()) {
                    recyclerView.smoothScrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading messages: " + databaseError.getMessage());
                progressBar.setVisibility(View.GONE);
            }
        };

        messagesRef.addValueEventListener(messagesListener);
    }

    private void markConversationAsRead() {
        DatabaseReference conversationRef = FirebaseDatabase.getInstance().getReference("conversations")
                .child(currentUserId).child(otherUserId);

        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("unread", false);

        conversationRef.updateChildren(updateMap);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();

        if (TextUtils.isEmpty(messageText)) {
            return;
        }

        // Clear input field
        messageInput.setText("");

        // Create message object
        Message message = new Message(currentUserId, otherUserId, messageText);
        long timestamp = new Date().getTime();
        message.setTimestamp(timestamp);

        // Get chat ID (consistent for both users)
        String chatId = getChatId(currentUserId, otherUserId);

        // Save message to database
        DatabaseReference chatMessagesRef = FirebaseDatabase.getInstance().getReference("messages").child(chatId);
        String messageId = chatMessagesRef.push().getKey();

        if (messageId != null) {
            message.setId(messageId);
            chatMessagesRef.child(messageId).setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        // Update current user's conversation
                        updateConversation(currentUserId, otherUserId, messageText, timestamp, false);

                        // Update other user's conversation
                        updateConversation(otherUserId, currentUserId, messageText, timestamp, true);
                    } else {
                        Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void updateConversation(String userId, String otherUserId, String lastMessage, long timestamp, boolean unread) {
        DatabaseReference conversationRef = FirebaseDatabase.getInstance().getReference("conversations")
                .child(userId).child(otherUserId);

        Map<String, Object> conversationMap = new HashMap<>();
        conversationMap.put("userId", otherUserId);
        conversationMap.put("lastMessage", lastMessage);
        conversationMap.put("timestamp", timestamp);
        conversationMap.put("unread", unread);

        conversationRef.updateChildren(conversationMap);
    }

    // Create a consistent chat ID for both users
    private String getChatId(String user1, String user2) {
        return user1.compareTo(user2) < 0 ? user1 + "_" + user2 : user2 + "_" + user1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove database listener to prevent memory leaks
        if (messagesRef != null && messagesListener != null) {
            messagesRef.removeEventListener(messagesListener);
        }
    }
}