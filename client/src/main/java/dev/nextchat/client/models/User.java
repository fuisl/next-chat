package dev.nextchat.client.models;

import java.util.UUID;

public class User {
    private UUID userId;
    private String username;
    private String password;

    public User() {}

    public User(UUID userId, String username, String password) {
        this.userId = userId;
        this.username = username;
        this.password = password;
    }

    // Getters & Setters
    public UUID getUserId() {
        return userId;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }

    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
}

