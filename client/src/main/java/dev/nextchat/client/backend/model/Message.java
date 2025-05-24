package dev.nextchat.client.backend.model;

import java.time.Instant;
import java.util.UUID;

public class Message {

    protected UUID id;
    private UUID senderId;
    private String senderName;
    protected UUID groupId;
    protected String message;
    protected Instant timestamp;

    // Constructors
    public Message() {}
    public Message(UUID senderId, String senderName, UUID groupId, String message, Instant timestamp) {
        this.id = UUID.randomUUID();
        this.senderName = senderName;
        this.senderId = senderId;
        this.groupId = groupId;
        this.message = message;
        this.timestamp = timestamp;
    }

    // Getters & Setter
    public UUID getId() { return id; }
    public UUID getSenderId() { return senderId; }
    public UUID getGroupId() { return groupId; }
    public String getMessage() { return message; }
    public Instant getTimestamp() { return timestamp; }
}