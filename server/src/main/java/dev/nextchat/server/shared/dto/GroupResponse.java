package dev.nextchat.server.shared.dto;

import java.time.Instant;
import java.util.UUID;

public class GroupResponse {
    private final UUID id;
    private final String name;
    private final String description;
    private final Instant createdAt;

    public GroupResponse(UUID id, String name, String description, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
