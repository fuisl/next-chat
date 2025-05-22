package dev.nextchat.client.models;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.Instant;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

public class ChatCell {

    private final StringProperty otherUsername;
    private UUID groupId;
    private final StringProperty lastMessage;
    private final ObjectProperty<Instant> timestamp;
    private final ObservableList<Message> messages = FXCollections.observableArrayList();

    public ChatCell(String initialOtherUsername, UUID groupId) {
        this.otherUsername = new SimpleStringProperty(this, "otherUsername", initialOtherUsername);
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

    // **** ADDED METHOD ****
    public void setOtherUsername(String username) {
        this.otherUsername.set(username);
    }

    public UUID getGroupId() {
        return groupId;
    }

    // Optional: Setter for groupId if it can change, though typically it wouldn't for a chat cell
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
        if (msg == null || msg.getId() == null) {
            return;
        }

        boolean messageExists = messages.stream().anyMatch(existingMsg -> existingMsg.getId().equals(msg.getId()));

        if (!messageExists) {
            messages.add(msg);
        } else {
            // Optional: If message with same ID exists, you might want to update it if content/timestamp can change.
        }
        updateLastMessageDetails(); // Update preview and sort after any potential change
    }

    public void updateLastMessageDetails() {
        if (!messages.isEmpty()) {
            messages.sort(Comparator.comparing(Message::getTimestamp, Comparator.nullsLast(Comparator.naturalOrder())));
            Message latestMessage = messages.get(messages.size() - 1);
            this.lastMessage.set(latestMessage.getMessage());
            this.timestamp.set(latestMessage.getTimestamp());
        } else {
            this.lastMessage.set("");
            this.timestamp.set(null);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatCell chatCell = (ChatCell) o;
        return Objects.equals(groupId, chatCell.groupId); // Usually, a ChatCell is unique by its groupId
    }
}