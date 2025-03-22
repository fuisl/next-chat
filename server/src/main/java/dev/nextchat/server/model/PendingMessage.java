package dev.nextchat.server.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document(collection = "pending_message")
public class PendingMessage extends Message{
    
    private UUID receiverId;

    // Constructors
    public PendingMessage() {}
    public PendingMessage(UUID receiverId, UUID groupId, String message, Instant timestamp) {
        this.id = UUID.randomUUID();
        this.receiverId = receiverId;
        this.groupId = groupId;
        this.message = message;
        this.timestamp = timestamp;
    }

    // Getters & Setters
    public UUID getId() { return id; }
    public UUID getSenderId() { return receiverId; }
    public UUID getGroupId() { return groupId; }
    public String getMessage() { return message; }
    public Instant getTimestamp() { return timestamp; }
}
