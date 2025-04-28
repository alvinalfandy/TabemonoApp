package com.example.aplikasichefai;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class WebVerificationActivity extends AppCompatActivity {

    private TextView statusText;
    private ProgressBar progressBar;
    private ImageView successIcon;
    private Button btnReturn;
    private DatabaseReference verificationRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_verification);

        // Initialize views
        statusText = findViewById(R.id.status_text);
        progressBar = findViewById(R.id.progress_bar);
        successIcon = findViewById(R.id.success_icon);
        btnReturn = findViewById(R.id.btn_return);

        // Initialize Firebase
        verificationRef = FirebaseDatabase.getInstance().getReference("email_verification_tokens");

        // Handle the intent (for deep links)
        handleIntent(getIntent());

        // Return to login button
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WebVerificationActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;

        Uri data = intent.getData();
        if (data != null) {
            // Check if it's from browser or app deep link
            String scheme = data.getScheme();
            String token = null;

            if ("https".equals(scheme)) {
                // From browser verification link
                token = data.getQueryParameter("token");
            } else if ("tabemono".equals(scheme)) {
                // From app deep link
                token = data.getQueryParameter("token");
            }

            if (token != null) {
                verifyToken(token);
            } else {
                showError("Invalid verification link");
            }
        }
    }

    private void verifyToken(String token) {
        statusText.setText("Verifying your email...");
        progressBar.setVisibility(View.VISIBLE);
        successIcon.setVisibility(View.GONE);
        btnReturn.setVisibility(View.GONE);

        // Check if token exists in Firebase
        verificationRef.child(token).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    showError("Invalid verification token");
                    return;
                }

                // Check if already used
                Boolean used = dataSnapshot.child("used").getValue(Boolean.class);
                if (used != null && used) {
                    showError("This verification link has already been used");
                    return;
                }

                // Get user ID
                String userId = dataSnapshot.child("userId").getValue(String.class);
                if (userId == null) {
                    showError("Invalid verification token");
                    return;
                }

                // Mark token as used
                verificationRef.child(token).child("used").setValue(true);
                verificationRef.child(token).child("verifiedAt").setValue(System.currentTimeMillis());

                // Update user record
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
                userRef.child("email_verified").setValue(true);

                // Show success message
                showSuccess("Email verified successfully! You can now log in to the app.");

                // Redirect to login after 3 seconds
                btnReturn.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(WebVerificationActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }, 3000);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showError("Error: " + databaseError.getMessage());
            }
        });
    }

    private void showSuccess(String message) {
        // Hide progress, show success icon
        progressBar.setVisibility(View.GONE);
        successIcon.setVisibility(View.VISIBLE);

        // Update text
        statusText.setText(message);

        // Show return button
        btnReturn.setVisibility(View.VISIBLE);
    }

    private void showError(String errorMessage) {
        // Hide progress, keep success icon hidden
        progressBar.setVisibility(View.GONE);
        successIcon.setVisibility(View.GONE);

        // Update text with error message
        statusText.setText(errorMessage);

        // Show return button
        btnReturn.setVisibility(View.VISIBLE);
    }
}