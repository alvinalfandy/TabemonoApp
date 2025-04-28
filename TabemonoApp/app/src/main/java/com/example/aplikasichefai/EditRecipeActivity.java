package com.example.aplikasichefai;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
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

public class EditRecipeActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "EditRecipeActivity";

    private EditText titleInput;
    private EditText descriptionInput;
    private TextInputLayout ingredientsInputLayout, stepsInputLayout;
    private Button saveButton;
    private ImageView recipeImageView, backButton;

    private String recipeId, imageUrl;
    private Uri imageUri;
    private ArrayList<String> ingredients, steps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recipe);

        // Initialize views
        titleInput = findViewById(R.id.titleInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        ingredientsInputLayout = findViewById(R.id.ingredientsInput);
        stepsInputLayout = findViewById(R.id.stepsInput);
        saveButton = findViewById(R.id.saveButton);
        recipeImageView = findViewById(R.id.recipeImageView);
        backButton = findViewById(R.id.backButton);

        // Setup back button
        backButton.setOnClickListener(v -> onBackPressed());

        // Get recipe data from intent
        Intent intent = getIntent();
        recipeId = intent.getStringExtra("recipeId");
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        imageUrl = intent.getStringExtra("imageUrl");
        ingredients = intent.getStringArrayListExtra("ingredients");
        steps = intent.getStringArrayListExtra("steps");

        // Initialize lists if null
        if (ingredients == null) ingredients = new ArrayList<>();
        if (steps == null) steps = new ArrayList<>();

        // Set data to views
        titleInput.setText(title);
        descriptionInput.setText(description);

        // Convert lists to new-line separated strings for input fields
        StringBuilder ingredientsBuilder = new StringBuilder();
        for (String ingredient : ingredients) {
            ingredientsBuilder.append(ingredient).append("\n");
        }
        ingredientsInputLayout.getEditText().setText(ingredientsBuilder.toString());

        StringBuilder stepsBuilder = new StringBuilder();
        for (String step : steps) {
            stepsBuilder.append(step).append("\n");
        }
        stepsInputLayout.getEditText().setText(stepsBuilder.toString());

        // Load image if available
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .centerCrop()
                    .into(recipeImageView);
        }

        // Setup image click listener
        recipeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        // Setup save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRecipe();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();

            // Display selected image
            Glide.with(this)
                    .load(imageUri)
                    .centerCrop()
                    .into(recipeImageView);
        }
    }

    private void saveRecipe() {
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String ingredientsText = ingredientsInputLayout.getEditText().getText().toString().trim();
        String stepsText = stepsInputLayout.getEditText().getText().toString().trim();

        // Validate input
        if (title.isEmpty()) {
            titleInput.setError("Title is required");
            titleInput.requestFocus();
            return;
        }

        // Convert text to ArrayLists
        ArrayList<String> validIngredients = new ArrayList<>(Arrays.asList(ingredientsText.split("\n")));
        ArrayList<String> validSteps = new ArrayList<>(Arrays.asList(stepsText.split("\n")));

        // Remove empty items
        validIngredients.removeIf(String::isEmpty);
        validSteps.removeIf(String::isEmpty);

        // Check if we have at least one ingredient and one step
        if (validIngredients.isEmpty()) {
            Toast.makeText(this, "Please add at least one ingredient", Toast.LENGTH_SHORT).show();
            return;
        }

        if (validSteps.isEmpty()) {
            Toast.makeText(this, "Please add at least one step", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading toast
        Toast.makeText(this, "Saving recipe...", Toast.LENGTH_SHORT).show();
        saveButton.setEnabled(false);
        saveButton.setText("Saving...");

        // If there's a new image, upload it first
        if (imageUri != null) {
            uploadImageToCustomServer(imageUri, title, description, validIngredients, validSteps);
        } else {
            // Otherwise update recipe directly with existing image URL
            updateRecipe(title, description, imageUrl, validIngredients, validSteps);
        }
    }

    private void uploadImageToCustomServer(Uri imageUri, final String title, final String description,
                                           final ArrayList<String> ingredients, final ArrayList<String> steps) {
        try {
            // Convert Uri to File
            File imageFile = createFileFromUri(imageUri);
            if (imageFile == null) {
                Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
                saveButton.setEnabled(true);
                saveButton.setText("Save Recipe");
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
                    .addFormDataPart("user_id", FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .build();

            // Add debugging
            final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
                        Toast.makeText(EditRecipeActivity.this,
                                "Upload failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        saveButton.setEnabled(true);
                        saveButton.setText("Save Recipe");
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
                                // Perbaikan URL - menambahkan "/api" jika tidak ada
                                if (imageUrl.contains("/uploads/") && !imageUrl.contains("/api/uploads/")) {
                                    imageUrl = imageUrl.replace("/uploads/", "/api/uploads/");
                                }
                                Log.d(TAG, "Success URL: " + imageUrl);
                                final String finalImageUrl = imageUrl;
                                runOnUiThread(() -> updateRecipe(title, description, finalImageUrl, ingredients, steps));
                            } else {
                                String message = jsonResponse.optString("message", "Unknown error");
                                Log.e(TAG, "Server error: " + message);
                                throw new Exception("Upload failed: " + message);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "JSON parsing error", e);
                            runOnUiThread(() -> {
                                Toast.makeText(EditRecipeActivity.this,
                                        "Failed to process server response: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                saveButton.setEnabled(true);
                                saveButton.setText("Save Recipe");
                            });
                        }
                    } else {
                        Log.e(TAG, "HTTP error: " + response.code() + " - " + responseBody);
                        runOnUiThread(() -> {
                            Toast.makeText(EditRecipeActivity.this,
                                    "Server error: " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                            saveButton.setEnabled(true);
                            saveButton.setText("Save Recipe");
                        });
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Exception in upload", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            saveButton.setEnabled(true);
            saveButton.setText("Save Recipe");
        }
    }

    // Helper method untuk konversi Uri ke File
    private File createFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                return null;
            }

            String fileName = "upload_" + System.currentTimeMillis() + ".jpg";
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

    // Kompres gambar sebelum upload
    private File compressImage(File originalFile) {
        try {
            // Decode image dari file
            Bitmap bitmap = BitmapFactory.decodeFile(originalFile.getAbsolutePath());

            if (bitmap == null) {
                Log.e(TAG, "Failed to decode bitmap from file");
                return originalFile;
            }

            // Resize gambar ke ukuran yang lebih kecil jika terlalu besar
            int maxSize = 1024; // Ukuran maksimal (width atau height)
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            float ratio = Math.min((float) maxSize / width, (float) maxSize / height);
            int newWidth = Math.round(width * ratio);
            int newHeight = Math.round(height * ratio);

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

            // Buat file baru untuk bitmap yang sudah dikompresi
            File compressedFile = new File(getCacheDir(), "compressed_" + originalFile.getName());
            FileOutputStream out = new FileOutputStream(compressedFile);

            // Kompres dengan kualitas 80% (bisa disesuaikan)
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.flush();
            out.close();

            Log.d(TAG, "Compressed image: " + originalFile.length() + " bytes to " + compressedFile.length() + " bytes");
            return compressedFile;
        } catch (Exception e) {
            Log.e(TAG, "Error compressing image", e);
            return originalFile; // Kembalikan file asli jika gagal
        }
    }

    private void updateRecipe(String title, String description, String imageUrl,
                              ArrayList<String> ingredients, ArrayList<String> steps) {
        // Get user ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Create database reference
        DatabaseReference recipeRef = FirebaseDatabase.getInstance()
                .getReference("user_recipes")
                .child(userId)
                .child(recipeId);

        // Create recipe map
        Map<String, Object> recipeUpdates = new HashMap<>();
        recipeUpdates.put("title", title);
        recipeUpdates.put("description", description);
        recipeUpdates.put("imageUrl", imageUrl);
        recipeUpdates.put("ingredients", ingredients);
        recipeUpdates.put("steps", steps);

        // Update recipe
        recipeRef.updateChildren(recipeUpdates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditRecipeActivity.this, "Recipe updated successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Return to previous activity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditRecipeActivity.this, "Failed to update recipe: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    saveButton.setEnabled(true);
                    saveButton.setText("Save Recipe");
                });
    }
}