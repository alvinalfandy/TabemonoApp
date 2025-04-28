package com.example.aplikasichefai;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private TextView registerLink;
    private TextView forgotPasswordLink;
    private FirebaseAuth auth;
    private DatabaseReference userRef;
    private DatabaseReference verificationRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("users");
        verificationRef = FirebaseDatabase.getInstance().getReference("email_verification_tokens");

        // Check if user is already logged in
        if (auth.getCurrentUser() != null) {
            // Check if email is verified in our database
            checkEmailVerified(auth.getCurrentUser().getUid());
        }

        // Initialize views
        inputEmail = findViewById(R.id.input_email);
        inputPassword = findViewById(R.id.input_password);
        btnLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progress_bar);
        registerLink = findViewById(R.id.register_link);
        forgotPasswordLink = findViewById(R.id.forgot_password_link);

        // Set up listeners
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        // Add click listener for forgot password
        forgotPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });
    }

    private void loginUser() {
        String emailOrUsername = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        if (TextUtils.isEmpty(emailOrUsername)) {
            inputEmail.setError("Email or username is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            inputPassword.setError("Password is required");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Check if input is email or username
        if (emailOrUsername.contains("@")) {
            // Login with email
            loginWithEmail(emailOrUsername, password);
        } else {
            // Login with username
            loginWithUsername(emailOrUsername, password);
        }
    }

    private void loginWithEmail(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Check if email is verified in our database
                            checkEmailVerified(auth.getCurrentUser().getUid());
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void loginWithUsername(String username, final String password) {
        // Query to find user by username
        Query query = userRef.orderByChild("username").equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null) {
                            // Found user with matching username, now login with their email
                            loginWithEmail(user.getEmail(), password);
                            return;
                        }
                    }
                }
                // No matching user found
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "No user found with this username", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkEmailVerified(String userId) {
        // Check our verification records to see if this user's email is verified
        Query verificationQuery = verificationRef.orderByChild("userId").equalTo(userId);
        verificationQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean isVerified = false;

                // Look through all verification tokens for this user
                for (DataSnapshot tokenSnapshot : dataSnapshot.getChildren()) {
                    if (tokenSnapshot.child("used").getValue(Boolean.class) != null &&
                            tokenSnapshot.child("used").getValue(Boolean.class)) {
                        isVerified = true;
                        break;
                    }
                }

                progressBar.setVisibility(View.GONE);

                if (isVerified) {
                    // Email verified, proceed to home
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Email not verified
                    Toast.makeText(LoginActivity.this,
                            "Please verify your email first. Check your inbox.",
                            Toast.LENGTH_LONG).show();

                    // Get user email for resending verification
                    userRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                User user = snapshot.getValue(User.class);
                                if (user != null) {
                                    // Offer to resend verification email
                                    showResendVerificationDialog(user.getUserId(), user.getEmail(), user.getName());
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });

                    // Sign out the unverified user
                    auth.signOut();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showResendVerificationDialog(final String userId, final String email, final String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Email Verification");
        builder.setMessage("Would you like to resend the verification email?");
        builder.setPositiveButton("Resend", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Generate new verification token
                String token = VerificationManager.generateVerificationToken(userId, email);
                String verificationLink = VerificationManager.generateVerificationLink(token);

                // Send verification email
                EmailSender.sendVerificationEmail(email, name, verificationLink);

                Toast.makeText(LoginActivity.this,
                        "Verification email sent again. Please check your inbox.",
                        Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}