package dev.nextchat.client.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Client {
    private final StringProperty userName;
    private final StringProperty fusername;

    public Client(final String userName, final String fusername) {
        this.userName = new SimpleStringProperty(this,"username",userName);
        this.fusername = new SimpleStringProperty(this,"fusername",fusername);
    }
    public StringProperty usernameProperty() {
        return this.userName;
    }
    public StringProperty fusernameProperty() {
        return this.fusername;
    }
}
