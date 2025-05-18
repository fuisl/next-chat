package dev.nextchat.client.backend.utils;

import dev.nextchat.client.models.Message;
import org.json.JSONObject;

import java.time.Instant;
import java.util.UUID;

public class MessageParser {

    public static Message fromJson(JSONObject json) {
        return new Message(
                UUID.fromString(json.getString("id")),
                UUID.fromString(json.getString("senderId")),
                UUID.fromString(json.getString("groupId")),
                json.getString("content"),
                Instant.parse(json.getString("timestamp"))

        );
    }

    public static JSONObject toJson(Message msg) {
        JSONObject json = new JSONObject();
        json.put("type", "message");
        json.put("id", msg.getId().toString());
        json.put("senderId", msg.getSenderId().toString());
        json.put("groupId", msg.getGroupId().toString());
        json.put("content", msg.getMessage());
        json.put("timestamp", msg.getTimestamp());
        return json;
    }
}
