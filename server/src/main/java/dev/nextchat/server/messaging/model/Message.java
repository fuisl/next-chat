package dev.nextchat.server.messaging.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document(collection = "messages")
public class Message {

    @Id
    private UUID id;
    private UUID groupId;
    private String senderUsername;
    private UUID senderId;
    private String content;
    private Instant timestamp;

    public Message() {
        // Required by JPA
    }

    public Message(UUID groupId,String senderUsername, UUID senderId, String content) {
        this.groupId = groupId;
        this.senderUsername = senderUsername;
        this.senderId = senderId;
        this.content = content;
        this.timestamp = Instant.now();
        this.id = UUID.randomUUID();
    }

    // test constructor for MessageServiceTest
    public Message(String senderUsername,UUID senderId, UUID groupId, String content, Instant timestamp) {
        this.senderUsername = senderUsername;
        this.senderId = senderId;
        this.groupId = groupId;
        this.content = content;
        this.timestamp = timestamp;
        this.id = UUID.randomUUID();
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public String getContent() {
        return content;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    // Setters (optional unless you plan to mutate values)
    public void setId(UUID id) {
        this.id = id;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }
}
