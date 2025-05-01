package dev.nextchat.client.models;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.Instant;

public class ChatCell {
    private final StringProperty sender;
    private final StringProperty txt_msg;
    private final ObjectProperty<Instant> timestamp;
    private final ObservableList<Message> messages = FXCollections.observableArrayList(); // Store msgs


    public ChatCell(final String sender, final String txt_msg, final Instant timestamp) {
        this.sender = new SimpleStringProperty(this,"Sender",sender);
        this.txt_msg = new SimpleStringProperty(this,"Msg",txt_msg);
        this.timestamp = new SimpleObjectProperty<>(this, "Timestamp", timestamp);

    }
    public StringProperty senderProperty() {
        return this.sender;
    }
    public StringProperty txtMsgProperty() {
        return this.txt_msg;
    }
    public ObjectProperty<Instant> timestampProperty() {
        return this.timestamp;
    }

    public ObservableList<Message> getMessages() {
        return messages;
    }
    public void addMessage(Message message) {
        messages.add(message);
        txt_msg.set(message.getMessage());
        timestamp.set(message.getTimestamp());
    }

}
