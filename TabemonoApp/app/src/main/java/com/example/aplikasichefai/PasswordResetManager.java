package com.example.aplikasichefai;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PasswordResetManager {

    private static final String RESET_NODE = "password_reset_tokens";
    private static DatabaseReference resetRef = FirebaseDatabase.getInstance().getReference(RESET_NODE);

    // Generate reset token and save it to Firebase
    public static String generateResetToken(String userId, String email) {
        String rawToken = UUID.randomUUID().toString();

        // Hash the token for security
        String hashedToken = hashToken(rawToken);

        // Save token in Firebase with user ID and email
        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("userId", userId);
        tokenData.put("email", email);
        tokenData.put("createdAt", System.currentTimeMillis());
        tokenData.put("used", false);
        tokenData.put("expiresAt", System.currentTimeMillis() + (24 * 60 * 60 * 1000)); // 24 hours expiry

        resetRef.child(hashedToken).setValue(tokenData);

        return hashedToken;
    }

    // Generate password reset link with your domain
    public static String generateResetLink(String token) {
        return "https://tabemono.my.id/password-reset.php?token=" + token;
    }

    // Hash token using SHA-256
    private static String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            // Fallback to a simpler approach if SHA-256 is not available
            return token.replaceAll("-", "").substring(0, 32);
        }
    }
}