package dev.nextchat.server.auth.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_account")
public class User {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)") // Store UUID as binary
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = true, updatable = true)
    private Instant last_online;

    // Constructors
    public User() {
    } // Required by JPA

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Instant getLastOnlineTimeStamp() {
        return last_online;
    }
}
