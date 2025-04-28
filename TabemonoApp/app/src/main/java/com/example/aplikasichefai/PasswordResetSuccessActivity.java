package com.example.aplikasichefai;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PasswordResetSuccessActivity extends AppCompatActivity {

    private TextView statusText;
    private Button btnReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset_success);

        // Initialize views
        statusText = findViewById(R.id.status_text);
        btnReturn = findViewById(R.id.btn_return);

        // Handle the intent (for deep links)
        handleIntent(getIntent());

        // Return to login button
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PasswordResetSuccessActivity.this, LoginActivity.class);
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
            String path = data.getPath();
            if (path != null) {
                if (path.contains("password-reset-success")) {
                    // Password reset was successful
                    statusText.setText("Your password has been successfully reset!");
                } else if (path.contains("password-reset-error")) {
                    // Password reset failed
                    statusText.setText("There was a problem resetting your password. Please try again.");
                }
            }
        }
    }
}