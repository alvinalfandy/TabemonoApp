package com.example.aplikasichefai;

/**
 * Class to hold both user and recipe data for feed items
 */
public class FeedItem {
    private User user;
    private Recipe recipe;

    // Default constructor for Firebase
    public FeedItem() {
    }

    public FeedItem(User user, Recipe recipe) {
        this.user = user;
        this.recipe = recipe;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }
}