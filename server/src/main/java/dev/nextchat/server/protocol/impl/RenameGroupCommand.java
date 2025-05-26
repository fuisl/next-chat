package dev.nextchat.server.protocol.impl;

import dev.nextchat.server.group.service.GroupService;
import dev.nextchat.server.messaging.service.RelayService;
import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandContext;
import dev.nextchat.server.protocol.CommandType;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;

public class RenameGroupCommand implements Command {

    private final String requestId;
    private final String name;
    private final UUID groupId;
    private final RelayService relayService;

    public RenameGroupCommand(String requestId, String groupId, String name, RelayService relayService) {
        this.requestId = requestId;
        this.groupId = !groupId.equals("") ? UUID.fromString(groupId) : null;
        this.name = name;
        this.relayService = relayService;
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

        // 📡 Relay to online group members (except sender)
        List<UUID> groupMembers = groupService.getUserIdsInGroup(groupId);
        JSONObject json = new JSONObject();
        json.put("type", "change_group_name");
        json.put("groupId", groupId.toString());
        json.put("name", name);

        for (UUID memberId : groupMembers) {
            if (!memberId.equals(userId)) {
                relayService.sendToUser(memberId, json.toString());
            }
        }

        response.put("status", "ok");
        response.put("message", "Renamed group to " + name);
        return response;
    }
}
