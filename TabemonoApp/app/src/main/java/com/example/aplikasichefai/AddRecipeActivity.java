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
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddRecipeActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "AddRecipeActivity";

    private Toolbar toolbar;
    private ImageView recipeImageView;
    private Button chooseImageButton;
    private TextInputLayout titleInput, descriptionInput, ingredientsInput, stepsInput;
    private Button saveButton;

    private Uri imageUri;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        recipeImageView = findViewById(R.id.recipeImageView);
        chooseImageButton = findViewById(R.id.chooseImageButton);
        titleInput = findViewById(R.id.titleInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        ingredientsInput = findViewById(R.id.ingredientsInput);
        stepsInput = findViewById(R.id.stepsInput);
        saveButton = findViewById(R.id.saveButton);

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add New Recipe");
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
                saveRecipe();
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
            Glide.with(this).load(imageUri).into(recipeImageView);
            recipeImageView.setVisibility(View.VISIBLE);
        }
    }

    private void saveRecipe() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        // Show loading indicator
        saveButton.setEnabled(false);
        saveButton.setText("Saving...");

        // Upload image if provided, otherwise use default
        if (imageUri != null) {
            uploadImageToCustomServer(imageUri);
        } else {
            // Default image URL dari server kamu
            String defaultImageUrl = "https://tabemono.my.id/images/default_recipe.jpg";
            saveRecipeToDatabase(defaultImageUrl);
        }
    }

    private void uploadImageToCustomServer(Uri imageUri) {
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
                        Toast.makeText(AddRecipeActivity.this,
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
                                runOnUiThread(() -> saveRecipeToDatabase(finalImageUrl));
                            } else {
                                String message = jsonResponse.optString("message", "Unknown error");
                                Log.e(TAG, "Server error: " + message);
                                throw new Exception("Upload failed: " + message);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "JSON parsing error", e);
                            runOnUiThread(() -> {
                                Toast.makeText(AddRecipeActivity.this,
                                        "Failed to process server response: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                saveButton.setEnabled(true);
                                saveButton.setText("Save Recipe");
                            });
                        }
                    } else {
                        Log.e(TAG, "HTTP error: " + response.code() + " - " + responseBody);
                        runOnUiThread(() -> {
                            Toast.makeText(AddRecipeActivity.this,
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

    private void saveRecipeToDatabase(String imageUrl) {
        String title = titleInput.getEditText().getText().toString().trim();
        String description = descriptionInput.getEditText().getText().toString().trim();
        String ingredientsText = ingredientsInput.getEditText().getText().toString().trim();
        String stepsText = stepsInput.getEditText().getText().toString().trim();

        // Convert ingredients and steps strings to ArrayLists
        ArrayList<String> ingredients = new ArrayList<>(Arrays.asList(ingredientsText.split("\n")));
        ArrayList<String> steps = new ArrayList<>(Arrays.asList(stepsText.split("\n")));

        // Create recipe object
        Recipe recipe = new Recipe(title, imageUrl, description, ingredients, steps);

        // Generate unique key for the recipe
        String recipeId = databaseReference.child("user_recipes").push().getKey();

        // Save the recipe under the user's ID
        databaseReference.child("user_recipes")
                .child(auth.getCurrentUser().getUid())
                .child(recipeId)
                .setValue(recipe)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(AddRecipeActivity.this, "Recipe saved successfully!", Toast.LENGTH_SHORT).show();
                            finish(); // Return to previous screen
                        } else {
                            Toast.makeText(AddRecipeActivity.this, "Failed to save recipe: " +
                                            (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                    Toast.LENGTH_SHORT).show();
                            saveButton.setEnabled(true);
                            saveButton.setText("Save Recipe");
                        }
                    }
                });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String title = titleInput.getEditText().getText().toString().trim();
        if (title.isEmpty()) {
            titleInput.setError("Title cannot be empty");
            isValid = false;
        } else {
            titleInput.setError(null);
        }

        String description = descriptionInput.getEditText().getText().toString().trim();
        if (description.isEmpty()) {
            descriptionInput.setError("Description cannot be empty");
            isValid = false;
        } else {
            descriptionInput.setError(null);
        }

        String ingredients = ingredientsInput.getEditText().getText().toString().trim();
        if (ingredients.isEmpty()) {
            ingredientsInput.setError("Please add at least one ingredient");
            isValid = false;
        } else {
            ingredientsInput.setError(null);
        }

        String steps = stepsInput.getEditText().getText().toString().trim();
        if (steps.isEmpty()) {
            stepsInput.setError("Please add at least one step");
            isValid = false;
        } else {
            stepsInput.setError(null);
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