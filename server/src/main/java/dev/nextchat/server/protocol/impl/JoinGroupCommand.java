package dev.nextchat.server.protocol.impl;

import dev.nextchat.server.group.service.GroupService;
import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandContext;
import dev.nextchat.server.protocol.CommandType;

import org.json.JSONObject;

import java.util.UUID;

public class JoinGroupCommand implements Command {
    private final String groupId;

    public JoinGroupCommand(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public CommandType getType() {
        return CommandType.JOIN_GROUP;
    }

    @Override
    public JSONObject execute(CommandContext context) {
        JSONObject response = new JSONObject();

        if (!context.isAuthenticated()) {
            response.put("type", "join_group_response");
            response.put("status", "error");
            response.put("message", "User is not authenticated");
            return response;
        }

        UUID userId = context.sessionUserId();
        GroupService groupService = context.groupService();

        try {
            UUID groupUUID = UUID.fromString(groupId);
            boolean joined = groupService.addUserToGroup(groupUUID, userId);

            response.put("type", "join_group_response");
            response.put("status", joined ? "ok" : "already_joined");
            response.put("message", joined ? "Successfully joined group" : "User already in group");
        } catch (IllegalArgumentException e) {
            response.put("type", "join_group_response");
            response.put("status", "error");
            response.put("message", "Invalid group ID format");
        } catch (IllegalStateException | UnsupportedOperationException e) {
            response.put("type", "join_group_response");
            response.put("status", "error");
            response.put("message", "Failed to join group: " + e.getMessage());
        }

        return response;
    }
}
