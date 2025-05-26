package dev.nextchat.server.protocol.impl;

import dev.nextchat.server.group.service.GroupService;
import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandContext;
import dev.nextchat.server.protocol.CommandType;
import org.json.JSONObject;

import java.util.UUID;

public class LeaveGroupCommand implements Command {

    private final String requestId;
    private final UUID groupId;

    public LeaveGroupCommand(String requestId, String groupId) {
        this.requestId = requestId;
        this.groupId = !groupId.equals("") ? UUID.fromString(groupId) : null;
    }

    @Override
    public CommandType getType() {
        return CommandType.LEAVE_GROUP;
    }

    @Override
    public JSONObject execute(CommandContext context) {
        GroupService groupService = context.groupService();
        JSONObject response = new JSONObject();
        response.put("requestId", requestId);
        response.put("type", "leave_group_response");
        response.put("groupId", groupId);

        if (!context.isAuthenticated()) {
            response.put("status", "error");
            response.put("message", "Authentication required");
            return response;
        }

        UUID userId = context.sessionUserId();
        response.put("userId", userId);

        int status = groupService.leaveGroup(userId, groupId);

        if (status == 0) {
            response.put("status", "error");
            response.put("message", "User not in group!");
            return response;
        }

        if (status != 1) {
            response.put("status", "error");
            response.put("message", "Something went wrong?");
            return response;
        }

        response.put("status", "ok");
        response.put("message", "Left group!");
        return response;
    }
}
