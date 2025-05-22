package dev.nextchat.client.models;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.Instant;
import java.util.UUID;

public class ChatCell {
    private final StringProperty otherUsername;
    private UUID groupId;
    private final StringProperty lastMessage;
    private final ObjectProperty<Instant> timestamp;
    private final ObservableList<Message> messages = FXCollections.observableArrayList();

    public ChatCell(String otherUsername, UUID groupId) {
        this.otherUsername = new SimpleStringProperty(this, "otherUsername", otherUsername);
        this.lastMessage = new SimpleStringProperty(this, "lastMessage", "");
        this.timestamp = new SimpleObjectProperty<>(this, "timestamp", null);
        this.groupId = groupId;
    }

    public String getOtherUsername() {
        return otherUsername.get();
    }
    public StringProperty otherUsernameProperty() {
        return otherUsername;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public ObservableList<Message> getMessages() {
        return messages;
    }

    public StringProperty lastMessageProperty() {
        return lastMessage;
    }

    public ObjectProperty<Instant> timestampProperty() {
        return timestamp;
    }

    public void addMessage(Message msg) {
        messages.add(msg);
        lastMessage.set(msg.getMessage());
        timestamp.set(msg.getTimestamp());
    }
}
