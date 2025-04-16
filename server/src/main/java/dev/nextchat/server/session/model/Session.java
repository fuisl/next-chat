package dev.nextchat.server.session.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "session_entry")
public class Session {

    @Id
    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private Instant loginTime;

    public Session() {
        // Default constructor for JPA
    }

    public Session(String token, UUID userId, Instant loginTime) {
        this.token = token;
        this.userId = userId;
        this.loginTime = loginTime;
    }

    // Getters
    public String getToken() {
        return token;
    }

    public UUID getUserId() {
        return userId;
    }

    public Instant getLoginTime() {
        return loginTime;
    }

    // Setters
    public void setToken(String token) {
        this.token = token;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setLoginTime(Instant loginTime) {
        this.loginTime = loginTime;
    }
}
