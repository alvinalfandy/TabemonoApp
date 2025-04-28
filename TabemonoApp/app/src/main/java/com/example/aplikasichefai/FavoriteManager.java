package com.example.aplikasichefai;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FavoriteManager {
    private static final String PREFS_NAME = "FavoriteRecipes";
    private static final String FAVORITES_KEY = "favorite_recipes";
    private SharedPreferences sharedPreferences;
    private Gson gson;

    public FavoriteManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // Save favorites to SharedPreferences
    public void saveFavorites(List<Recipe> favoriteRecipes) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = gson.toJson(favoriteRecipes);
        editor.putString(FAVORITES_KEY, json);
        editor.apply();
    }

    // Get favorites from SharedPreferences
    public List<Recipe> getFavorites() {
        String json = sharedPreferences.getString(FAVORITES_KEY, null);
        Type type = new TypeToken<ArrayList<Recipe>>() {}.getType();
        List<Recipe> favorites = gson.fromJson(json, type);
        return favorites != null ? favorites : new ArrayList<>();
    }

    // Add a recipe to favorites
    public void addToFavorites(Recipe recipe) {
        List<Recipe> favorites = getFavorites();

        // Check if recipe already exists to avoid duplicates
        boolean exists = favorites.stream()
                .anyMatch(r -> r.getTitle().equals(recipe.getTitle()));

        if (!exists) {
            favorites.add(recipe);
            saveFavorites(favorites);
        }
    }

    // Remove a recipe from favorites
    public void removeFromFavorites(Recipe recipe) {
        List<Recipe> favorites = getFavorites();
        favorites.removeIf(r -> r.getTitle().equals(recipe.getTitle()));
        saveFavorites(favorites);
    }

    // Check if a recipe is in favorites
    public boolean isFavorite(Recipe recipe) {
        List<Recipe> favorites = getFavorites();
        return favorites.stream()
                .anyMatch(r -> r.getTitle().equals(recipe.getTitle()));
    }
}