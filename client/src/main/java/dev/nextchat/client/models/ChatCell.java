package dev.nextchat.client.models;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


import java.time.LocalDateTime;

public class ChatCell {
    private final StringProperty sender;
    private final StringProperty txt_msg;
    private final ObjectProperty<LocalDateTime> timestamp;

    public ChatCell(final String sender, final String txt_msg, final LocalDateTime timestamp) {
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
    public ObjectProperty<LocalDateTime> timestampProperty() {
        return this.timestamp;
    }


}
