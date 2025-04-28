package com.example.aplikasichefai;

public class BannerItem {
    private String imageUrl;
    private String recipeId;

    // Empty constructor needed for Firebase
    public BannerItem() {
    }

    public BannerItem(String imageUrl, String recipeId) {
        this.imageUrl = imageUrl;
        this.recipeId = recipeId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }
}