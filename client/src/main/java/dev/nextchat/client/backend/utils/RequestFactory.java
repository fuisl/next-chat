package dev.nextchat.client.backend.utils;

import java.time.Instant;
import java.util.UUID;

import dev.nextchat.client.models.Message;
import org.json.JSONObject;

/*
 * This is a helper class for creating valid json messages based on the 
 * predefined protocol for communication between client and server. The
 * commands available here are based on the commit 70e14f91030... of pull request 18.
 * These methods should be the only way the client UI uses to send messages to the server,
 * which can be done by using the desired method to generate the JSONObject
 * and push to the SendingMessageQueue.
 * @see <a href="https://github.com/fuisl/next-chat/pull/18">Pull request 18</a>
*/

public class RequestFactory {
    public static JSONObject createMessageRequest(Message msg) {
        JSONObject json = new JSONObject();
        json.put("type", "message");
        json.put("id", msg.getId().toString());
        json.put("senderId", msg.getSenderId().toString());
        json.put("groupId", msg.getGroupId().toString());
        json.put("content", msg.getMessage());
        json.put("timestamp", msg.getTimestamp());
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
    public static JSONObject checkIfUserExist(String username) {
        JSONObject json = new JSONObject();
        json.put("type", "checkUserExistence");
        json.put("username", username);
        return json;
    }

}
