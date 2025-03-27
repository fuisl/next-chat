package dev.nextchat.client.models;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDate;

public class ChatCell {
    private final StringProperty sender;
    private final StringProperty txt_msg;
    private final ObjectProperty<LocalDate> date;

    public ChatCell(final String sender, final String txt_msg, final LocalDate date) {
        this.sender = new SimpleStringProperty(this,"Sender",sender);
        this.txt_msg = new SimpleStringProperty(this,"Msg",txt_msg);
        this.date = new SimpleObjectProperty<>(this,"Date",date);
    }
    public StringProperty senderProperty() {
        return this.sender;
    }
    public StringProperty txtMsgProperty() {
        return this.txt_msg;
    }
    public ObjectProperty<LocalDate> dateProperty() {
        return this.date;
    }


}
