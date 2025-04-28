package com.example.aplikasichefai;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "EditProfileActivity";

    private Toolbar toolbar;
    private ImageView profileImageView;
    private Button chooseImageButton;
    private TextInputLayout nameInput, bioInput;
    private Button saveButton;

    private Uri imageUri;
    private FirebaseAuth auth;
    private DatabaseReference userRef;
    private String currentProfileImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            // If not logged in, go back to login
            startActivity(new Intent(EditProfileActivity.this, LoginActivity.class));
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        profileImageView = findViewById(R.id.profileImageView);
        chooseImageButton = findViewById(R.id.chooseImageButton);
        nameInput = findViewById(R.id.nameInput);
        bioInput = findViewById(R.id.bioInput);
        saveButton = findViewById(R.id.saveButton);

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Profile");
        }

        // Setup image picker
        chooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        // Setup save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfile();
            }
        });

        // Load current user data
        loadUserData();
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        // Populate fields with current values
                        nameInput.getEditText().setText(user.getName());

                        // Set bio if it exists
                        if (user.getBio() != null) {
                            bioInput.getEditText().setText(user.getBio());
                        }

                        // Save current profile image URL
                        currentProfileImageUrl = user.getProfileImageUrl();

                        // Load profile image if available
                        if (currentProfileImageUrl != null && !currentProfileImageUrl.isEmpty()) {
                            Glide.with(EditProfileActivity.this)
                                    .load(currentProfileImageUrl)
                                    .circleCrop()
                                    .into(profileImageView);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading user data: " + databaseError.getMessage());
                Toast.makeText(EditProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).circleCrop().into(profileImageView);
        }
    }

    private void saveProfile() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        // Show loading indicator
        saveButton.setEnabled(false);
        saveButton.setText("Saving...");

        // Upload image if provided, otherwise use existing or default
        if (imageUri != null) {
            uploadImageToCustomServer(imageUri);
        } else {
            // Use existing image URL or default
            String imageUrl = (currentProfileImageUrl != null && !currentProfileImageUrl.isEmpty())
                    ? currentProfileImageUrl
                    : "https://tabemono.my.id/images/default_profile.jpg";
            updateUserProfile(imageUrl);
        }
    }

    private void uploadImageToCustomServer(Uri imageUri) {
        try {
            // Convert Uri to File
            File imageFile = createFileFromUri(imageUri);
            if (imageFile == null) {
                Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
                saveButton.setEnabled(true);
                saveButton.setText("Save Profile");
                return;
            }

            // Compress image to save bandwidth and storage
            File compressedFile = compressImage(imageFile);

            // Create OkHttpClient with longer timeouts
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            // Create multipart request body
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", compressedFile.getName(),
                            RequestBody.create(MediaType.parse("image/jpeg"), compressedFile))
                    .addFormDataPart("user_id", auth.getCurrentUser().getUid())
                    .build();

            // Add debugging
            final String userId = auth.getCurrentUser().getUid();
            final String fileName = compressedFile.getName();
            final long fileSize = compressedFile.length();

            // Log debug info
            Log.d(TAG, "Uploading: " + fileName + ", size: " + fileSize + ", user: " + userId);

            // Create request
            Request request = new Request.Builder()
                    .url("https://tabemono.my.id/api/upload_image.php")
                    .post(requestBody)
                    .build();

            // Send request asynchronously
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Failure: " + e.getMessage(), e);
                    runOnUiThread(() -> {
                        Toast.makeText(EditProfileActivity.this,
                                "Upload failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        saveButton.setEnabled(true);
                        saveButton.setText("Save Profile");
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Response code: " + response.code());
                    Log.d(TAG, "Response body: " + responseBody);

                    if (response.isSuccessful()) {
                        // Make sure server returns image URL in JSON format
                        try {
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            String status = jsonResponse.optString("status", "");

                            if (status.equals("success")) {
                                String imageUrl = jsonResponse.getString("url");
                                // Fix URL - add "/api" if it's missing
                                if (imageUrl.contains("/uploads/") && !imageUrl.contains("/api/uploads/")) {
                                    imageUrl = imageUrl.replace("/uploads/", "/api/uploads/");
                                }
                                Log.d(TAG, "Success URL: " + imageUrl);
                                final String finalImageUrl = imageUrl;
                                runOnUiThread(() -> updateUserProfile(finalImageUrl));
                            } else {
                                String message = jsonResponse.optString("message", "Unknown error");
                                Log.e(TAG, "Server error: " + message);
                                throw new Exception("Upload failed: " + message);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "JSON parsing error", e);
                            runOnUiThread(() -> {
                                Toast.makeText(EditProfileActivity.this,
                                        "Failed to process server response: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                saveButton.setEnabled(true);
                                saveButton.setText("Save Profile");
                            });
                        }
                    } else {
                        Log.e(TAG, "HTTP error: " + response.code() + " - " + responseBody);
                        runOnUiThread(() -> {
                            Toast.makeText(EditProfileActivity.this,
                                    "Server error: " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                            saveButton.setEnabled(true);
                            saveButton.setText("Save Profile");
                        });
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Exception in upload", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            saveButton.setEnabled(true);
            saveButton.setText("Save Profile");
        }
    }

    // Helper method for converting Uri to File
    private File createFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                return null;
            }

            String fileName = "profile_" + System.currentTimeMillis() + ".jpg";
            File outputFile = new File(getCacheDir(), fileName);

            OutputStream outputStream = new FileOutputStream(outputFile);
            byte[] buffer = new byte[4 * 1024]; // 4k buffer
            int read;

            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }

            outputStream.flush();
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return outputFile;
        } catch (Exception e) {
            Log.e(TAG, "Error creating file from URI", e);
            return null;
        }
    }

    // Compress image before upload
    private File compressImage(File originalFile) {
        try {
            // Decode image from file
            Bitmap bitmap = BitmapFactory.decodeFile(originalFile.getAbsolutePath());

            if (bitmap == null) {
                Log.e(TAG, "Failed to decode bitmap from file");
                return originalFile;
            }

            // Resize image if it's too large
            int maxSize = 1024; // Max size (width or height)
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            float ratio = Math.min((float) maxSize / width, (float) maxSize / height);
            int newWidth = Math.round(width * ratio);
            int newHeight = Math.round(height * ratio);

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

            // Create new file for compressed bitmap
            File compressedFile = new File(getCacheDir(), "compressed_" + originalFile.getName());
            FileOutputStream out = new FileOutputStream(compressedFile);

            // Compress with 80% quality
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.flush();
            out.close();

            Log.d(TAG, "Compressed image: " + originalFile.length() + " bytes to " + compressedFile.length() + " bytes");
            return compressedFile;
        } catch (Exception e) {
            Log.e(TAG, "Error compressing image", e);
            return originalFile; // Return original file if compression fails
        }
    }

    private void updateUserProfile(String imageUrl) {
        String name = nameInput.getEditText().getText().toString().trim();
        String bio = bioInput.getEditText().getText().toString().trim();

        // Create a map of fields to update
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("bio", bio);
        updates.put("profileImageUrl", imageUrl);

        userRef.updateChildren(updates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                            finish(); // Return to previous screen
                        } else {
                            Toast.makeText(EditProfileActivity.this, "Failed to update profile: " +
                                            (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                    Toast.LENGTH_SHORT).show();
                            saveButton.setEnabled(true);
                            saveButton.setText("Save Profile");
                        }
                    }
                });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String name = nameInput.getEditText().getText().toString().trim();
        if (name.isEmpty()) {
            nameInput.setError("Name cannot be empty");
            isValid = false;
        } else {
            nameInput.setError(null);
        }

        return isValid;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}