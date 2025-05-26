package dev.nextchat.server.protocol.impl;

import dev.nextchat.server.group.service.GroupService;
import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandContext;
import dev.nextchat.server.protocol.CommandType;
import org.json.JSONObject;

import java.util.UUID;

public class RenameGroupCommand implements Command {

    private final String requestId;
    private final String name;
    private final UUID groupId;

    public RenameGroupCommand(String requestId, String groupId, String name) {
        this.requestId = requestId;
        this.groupId = !groupId.equals("") ? UUID.fromString(groupId) : null;
        this.name = name;
    }

    @Override
    public CommandType getType() {
        return CommandType.RENAME_GROUP;
    }

    @Override
    public JSONObject execute(CommandContext context) {
        GroupService groupService = context.groupService();
        JSONObject response = new JSONObject();
        response.put("requestId", requestId);
        response.put("type", "rename_group_response");
        response.put("groupId", groupId);

        if (!context.isAuthenticated()) {
            response.put("status", "error");
            response.put("message", "Authentication required");
            return response;
        }

        if (groupId == null) {
            response.put("status", "error");
            response.put("message", "Invalid groupId!");
            return response;
        }

        UUID userId = context.sessionUserId();
        response.put("userId", userId);

        if (!groupService.isUserInGroup(groupId, userId)) {
            response.put("status", "error");
            response.put("message", "User is not a member of the group");
            return response;
        }

        int status = groupService.renameGroup(name, groupId);

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
        response.put("message", "Renamed group to " + name);
        response.put("name", name);
        return response;
    }
}
