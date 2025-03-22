package dev.nextchat.server.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document(collection = "received_message")
public class ReceivedMessage extends Message {
    
    private UUID senderId;

    // Constructors
    public ReceivedMessage() {}
    public ReceivedMessage(UUID senderId, UUID groupId, String message, Instant timestamp) {
        this.id = UUID.randomUUID();
        this.senderId = senderId;
        this.groupId = groupId;
        this.message = message;
        this.timestamp = timestamp;
    }

    // Getters & Setters
    public UUID getId() { return id; }
    public UUID getSenderId() { return senderId; }
    public UUID getGroupId() { return groupId; }
    public String getMessage() { return message; }
    public Instant getTimestamp() { return timestamp; }
}