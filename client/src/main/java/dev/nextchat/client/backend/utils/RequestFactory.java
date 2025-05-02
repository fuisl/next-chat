package dev.nextchat.client.backend.utils;

import java.time.Instant;
import java.util.UUID;

import org.json.JSONObject;

public class RequestFactory {
    public static JSONObject createMessageRequest(UUID userId, UUID groupId, String content) {
        JSONObject json = new JSONObject();
        json.put("type", "message");
        json.put("groupId", groupId.toString());
        json.put("senderId", userId.toString());
        json.put("content", content);
        json.put("timestamp", Instant.now());

        return json;
    }

    public static JSONObject createLoginRequest(String userName, String raw_password) {
        JSONObject json = new JSONObject();
        json.put("type", "login");
        json.put("username", userName);
        json.put("passwd", raw_password);
        return json;
    }

    public static JSONObject createSignupRequest(String userName, String raw_password) {
        JSONObject json = new JSONObject();
        json.put("type", "signup");
        json.put("username", userName);
        json.put("passwd", raw_password);
        return json;
    }

    public static JSONObject createNewGroupRequest(UUID userId, String groupName, String description) {
        JSONObject json = new JSONObject();
        json.put("type", "create_group");
        json.put("name", groupName);
        json.put("description", description);
        json.put("timestamp", Instant.now());
        return json;
    }

    public static JSONObject createJoinGroupRequest(UUID userId, UUID groupId) {
        JSONObject json = new JSONObject();
        json.put("type", "join_group");
        json.put("userId", userId);
        json.put("groupId", groupId);
        return json;
    }

    public static JSONObject createFetchRecentRequest(UUID groupId) {
        JSONObject json = new JSONObject();
        json.put("type", "fetch_recent");
        json.put("groupId", groupId);
        return json;
    }

    public static JSONObject createFetchBeforeTimeStampRequest(UUID groupId, Instant timestamp) {
        JSONObject json = new JSONObject();
        json.put("type", "fetch_before");
        json.put("groupId", groupId);
        json.put("timestamp", timestamp);
        return json;
    }
}
