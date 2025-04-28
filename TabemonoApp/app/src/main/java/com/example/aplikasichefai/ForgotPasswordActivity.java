package com.example.aplikasichefai;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText inputEmailOrUsername;
    private Button btnResetPassword;
    private ProgressBar progressBar;
    private TextView backToLogin;
    private FirebaseAuth auth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("users");

        // Initialize views
        inputEmailOrUsername = findViewById(R.id.input_email_username);
        btnResetPassword = findViewById(R.id.btn_reset_password);
        progressBar = findViewById(R.id.progress_bar);
        backToLogin = findViewById(R.id.back_to_login);

        // Set up listeners
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });

        backToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to login activity
            }
        });
    }

    private void resetPassword() {
        String emailOrUsername = inputEmailOrUsername.getText().toString().trim();

        if (TextUtils.isEmpty(emailOrUsername)) {
            inputEmailOrUsername.setError("Email or username is required");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Check if input is email or username
        if (emailOrUsername.contains("@")) {
            // Reset with email
            findUserByEmail(emailOrUsername);
        } else {
            // Look up email by username
            findUserByUsername(emailOrUsername);
        }
    }

    private void findUserByEmail(final String email) {
        // Query to find user by email
        Query emailQuery = userRef.orderByChild("email").equalTo(email);
        emailQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null) {
                            // Found user with matching email, send reset email
                            sendPasswordResetEmail(user.getUserId(), user.getEmail(), user.getName());
                            return;
                        }
                    }
                } else {
                    // No matching user found
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ForgotPasswordActivity.this,
                            "No account found with this email",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ForgotPasswordActivity.this,
                        "Database error: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void findUserByUsername(String username) {
        // Query to find user by username
        Query usernameQuery = userRef.orderByChild("username").equalTo(username);
        usernameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null && user.getEmail() != null) {
                            // Found user with matching username, send reset email
                            sendPasswordResetEmail(user.getUserId(), user.getEmail(), user.getName());
                            return;
                        }
                    }
                } else {
                    // No matching user found
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ForgotPasswordActivity.this,
                            "No user found with this username",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ForgotPasswordActivity.this,
                        "Database error: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendPasswordResetEmail(String userId, String email, String name) {
        // Generate new reset token
        String token = PasswordResetManager.generateResetToken(userId, email);
        String resetLink = PasswordResetManager.generateResetLink(token);

        // Send password reset email
        EmailSender.sendPasswordResetEmail(email, name, resetLink);

        progressBar.setVisibility(View.GONE);
        Toast.makeText(ForgotPasswordActivity.this,
                "Password reset instructions sent to your email",
                Toast.LENGTH_LONG).show();

        // Return to login after 2 seconds
        btnResetPassword.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 2000);
    }
}