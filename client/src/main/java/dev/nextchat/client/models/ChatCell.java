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

    public void addMessage(Message newMessage) {
        if (newMessage == null || newMessage.getGroupId() == null ||
                newMessage.getSenderId() == null || newMessage.getTimestamp() == null ||
                newMessage.getMessage() == null) { // Ensure all parts of our composite key are present
            System.err.println("[ChatCell.addMessage] Received message with null key components, cannot de-duplicate or add. Client-gen ID: " +
                    (newMessage != null ? newMessage.getId() : "null message object"));
            return;
        }

        boolean messageExists = false;
        for (Message existingMessage : this.messages) { // 'this.messages' is your ObservableList in ChatCell
            if (// No need to check groupId here as this method is on a specific ChatCell for a specific group
                    existingMessage.getSenderId().equals(newMessage.getSenderId()) &&
                            existingMessage.getTimestamp().equals(newMessage.getTimestamp()) && // Compare Instant objects
                            existingMessage.getMessage().equals(newMessage.getMessage())) {
                messageExists = true;
                break;
            }
        }

        if (!messageExists) {
            this.messages.add(newMessage);
            System.out.println("[ChatCell.addMessage] Added message (content: '" +
                    newMessage.getMessage().substring(0, Math.min(20, newMessage.getMessage().length())).replace("\n", " ") +
                    "...', client-gen ID: " + newMessage.getId() + ") to UI for group " + newMessage.getGroupId());
        } else {
            System.out.println("[ChatCell.addMessage] Duplicate message (sender/timestamp/content match) for group " +
                    newMessage.getGroupId() + ". Not re-adding to UI list. Client-gen ID of current instance: " + newMessage.getId());
        }
        updateLastMessageDetails();
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