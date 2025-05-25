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

    public String getOtherUsername() { return otherUsername.get(); }
    public StringProperty otherUsernameProperty() { return otherUsername; }
    public void setOtherUsername(String username) { this.otherUsername.set(username); }
    public UUID getGroupId() { return groupId; }
    public void setGroupId(UUID groupId) { this.groupId = groupId; }
    public ObservableList<Message> getMessages() { return messages; }
    public StringProperty lastMessageProperty() { return lastMessage; }
    public ObjectProperty<Instant> timestampProperty() { return timestamp; }

    public void addMessage(Message newMessage) {
        if (newMessage == null || newMessage.getGroupId() == null ||
                newMessage.getSenderId() == null || newMessage.getTimestamp() == null ||
                newMessage.getMessage() == null) {
            return;
        }
        boolean exists = false;
        for (Message m : messages) {
            if (m.getSenderId().equals(newMessage.getSenderId()) &&
                    m.getTimestamp().equals(newMessage.getTimestamp()) &&
                    m.getMessage().equals(newMessage.getMessage())) {
                exists = true;
                break;
            }
        }
        if (!exists) messages.add(newMessage);
        updateLastMessageDetails();
    }

    public void updateLastMessageDetails() {
        if (!messages.isEmpty()) {
            messages.sort(Comparator.comparing(Message::getTimestamp, Comparator.nullsLast(Comparator.naturalOrder())));
            Message latest = messages.get(messages.size() - 1);
            lastMessage.set(latest.getMessage());
            timestamp.set(latest.getTimestamp());
        } else {
            lastMessage.set("");
            timestamp.set(null);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatCell chatCell = (ChatCell) o;
        return Objects.equals(groupId, chatCell.groupId);
    }
}
