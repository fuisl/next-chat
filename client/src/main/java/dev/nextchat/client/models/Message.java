package dev.nextchat.client.models;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable {
    private String sender;
    private String message;
    private String groupID;
    private LocalDateTime timestamp;

    public Message(String sender, String groupID, String message, LocalDateTime timestamp) {
        this.sender = sender;
        this.groupID = groupID;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public String getGroupID() {
        return groupID;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }


    // debug
    @Override
    public String toString() {
        return "[" + timestamp + "] " + sender + " -> " + groupID + ": " + message;
    }

}
