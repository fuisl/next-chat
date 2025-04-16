package dev.nextchat.server.auth.model;

public class Credential {
    private final String username;
    private final String rawPassword;

    public Credential(String username, String rawPassword) {
        this.username = username;
        this.rawPassword = rawPassword;
    }

    public String getUsername() {
        return username;
    }

    public String getRawPassword() {
        return rawPassword;
    }
}