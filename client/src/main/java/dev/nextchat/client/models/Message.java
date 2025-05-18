package dev.nextchat.client.models;

import org.json.JSONObject;

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

    public Message(UUID id, UUID senderId, UUID groupId, String message, Instant timestamp) {
        this.id = UUID.randomUUID();
        this.senderId = senderId;
        this.groupId = groupId;
        this.message = message;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public String getMessage() {
        return message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

}
