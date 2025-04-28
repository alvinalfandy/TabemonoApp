package com.example.aplikasichefai;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Recipe {
    private String id;
    private String title;
    private String imageUrl;
    private String description;
    private ArrayList<String> ingredients;
    private ArrayList<String> steps;
    private Map<String, Float> ratings;  // Map of userId to rating value
    private float averageRating;        // Average rating calculation
    private int ratingCount;            // Number of ratings

    // Default constructor required for Firebase
    public Recipe() {
        this.ratings = new HashMap<>();
        this.averageRating = 0;
        this.ratingCount = 0;
    }

    // Original constructor (without id) for backward compatibility
    public Recipe(String title, String imageUrl, String description, ArrayList<String> ingredients, ArrayList<String> steps) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.description = description;
        this.ingredients = ingredients;
        this.steps = steps;
        this.ratings = new HashMap<>();
        this.averageRating = 0;
        this.ratingCount = 0;
    }

    // New constructor with id parameter
    public Recipe(String id, String title, String imageUrl, String description, ArrayList<String> ingredients, ArrayList<String> steps) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.description = description;
        this.ingredients = ingredients;
        this.steps = steps;
        this.ratings = new HashMap<>();
        this.averageRating = 0;
        this.ratingCount = 0;
    }

    // Getter and setter for id field
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Getter Methods
    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<String> getIngredients() {
        return ingredients;
    }

    public ArrayList<String> getSteps() {
        return steps;
    }

    public Map<String, Float> getRatings() {
        return ratings != null ? ratings : new HashMap<>();
    }

    public float getAverageRating() {
        // Recalculate average in case we access it directly
        if (ratings != null && !ratings.isEmpty()) {
            float sum = 0;
            for (Float value : ratings.values()) {
                if (value != null) {
                    sum += value;
                }
            }
            return ratings.size() > 0 ? sum / ratings.size() : 0;
        }
        return averageRating;
    }

    public int getRatingCount() {
        // Make sure count reflects actual number of ratings
        return ratings != null ? ratings.size() : ratingCount;
    }

    // Setter Methods
    public void setTitle(String title) {
        this.title = title;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIngredients(ArrayList<String> ingredients) {
        this.ingredients = ingredients;
    }

    public void setSteps(ArrayList<String> steps) {
        this.steps = steps;
    }

    public void setRatings(Map<String, Float> ratings) {
        this.ratings = ratings;
        updateRatingStats();
    }

    public void setAverageRating(float averageRating) {
        this.averageRating = averageRating;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    // Helper method to add a rating and recalculate average
    public void addRating(String userId, float rating) {
        if (this.ratings == null) {
            this.ratings = new HashMap<>();
        }

        // Add or update the rating
        this.ratings.put(userId, rating);

        // Update average and count
        updateRatingStats();
    }

    // Update rating statistics based on the ratings map
    private void updateRatingStats() {
        float sum = 0;
        if (ratings != null && !ratings.isEmpty()) {
            for (Float value : ratings.values()) {
                if (value != null) {
                    sum += value;
                }
            }
            this.ratingCount = ratings.size();
            this.averageRating = this.ratingCount > 0 ? sum / this.ratingCount : 0;
        } else {
            this.ratingCount = 0;
            this.averageRating = 0;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Recipe recipe = (Recipe) obj;

        if (id != null && id.equals(recipe.id)) {
            return true;
        }

        // Fall back to title comparison if id is not available
        return title != null && title.equals(recipe.title);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : (title != null ? title.hashCode() : 0);
    }
}