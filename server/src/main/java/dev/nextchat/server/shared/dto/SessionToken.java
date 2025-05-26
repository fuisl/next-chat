package dev.nextchat.server.shared.dto;

import java.util.UUID;

public class SessionToken {
    private final String token;
    private final UUID userId;

    public SessionToken(String token, UUID userId) {
        this.token = token;
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public UUID getUserId() {
        return userId;
    }
}
