package dev.nextchat.client.models;

import java.time.Instant;
import java.util.UUID;

public class Message {

    protected UUID id;
    private UUID senderId;
    protected UUID groupId;
    protected String message;
    protected Instant timestamp;

    // Constructors
    public Message() {}
    public Message(UUID senderId, UUID groupId, String message, Instant timestamp) {
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