package com.example.aplikasichefai;

public class User {
    private String userId;
    private String name;
    private String username;
    private String username_lower; // Added for case-insensitive search
    private String email;
    private String profileImageUrl;
    private String bio;
    private boolean email_verified;

    // Required empty constructor for Firebase
    public User() {
        // Default constructor required for Firebase
    }

    public User(String userId, String name, String username, String email) {
        this.userId = userId;
        this.name = name;
        this.username = username;
        if (username != null) {
            this.username_lower = username.toLowerCase();
        }
        this.email = email;
        this.email_verified = false; // Default to not verified
    }

    // Updated constructor with profileImageUrl
    public User(String userId, String name, String username, String email, String profileImageUrl) {
        this.userId = userId;
        this.name = name;
        this.username = username;
        if (username != null) {
            this.username_lower = username.toLowerCase();
        }
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.email_verified = false; // Default to not verified
    }

    // Updated constructor with all fields including bio and email_verified
    public User(String userId, String name, String username, String email, String profileImageUrl, String bio, boolean email_verified) {
        this.userId = userId;
        this.name = name;
        this.username = username;
        if (username != null) {
            this.username_lower = username.toLowerCase();
        }
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.bio = bio;
        this.email_verified = email_verified;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        // Also set lowercase version for case-insensitive search
        if (username != null) {
            this.username_lower = username.toLowerCase();
        }
    }

    public String getUsername_lower() {
        return username_lower;
    }

    public void setUsername_lower(String username_lower) {
        this.username_lower = username_lower;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Getter and setter for profileImageUrl
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public boolean isEmail_verified() {
        return email_verified;
    }

    public void setEmail_verified(boolean email_verified) {
        this.email_verified = email_verified;
    }
}