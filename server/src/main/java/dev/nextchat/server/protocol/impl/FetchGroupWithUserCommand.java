package dev.nextchat.server.protocol.impl;

import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandContext;
import dev.nextchat.server.protocol.CommandType;
import dev.nextchat.server.group.service.GroupService;

import org.json.JSONObject;

import java.util.Optional;
import java.util.UUID;

public class FetchGroupWithUserCommand implements Command {
    private final String requestId;
    private final UUID memberId;

    public FetchGroupWithUserCommand(String requestId, String memberId) {
        this.requestId = requestId;
        this.memberId = !memberId.equals("") ? UUID.fromString(memberId) : null;
    }

    @Override
    public CommandType getType() {
        return CommandType.FETCH_GROUP_WITH_USER;
    }

    @Override
    public JSONObject execute(CommandContext context) {
        GroupService groupService = context.groupService();
        JSONObject response = new JSONObject();
        response.put("requestId", requestId);
        response.put("status", "fetch_group_with_user_response");

        if (!context.isAuthenticated()) {
            response.put("status", "error");
            response.put("message", "Authentication required");
            return response;
        }

        UUID userId = context.sessionUserId();
        Optional<UUID> mutualGroupId = groupService.getGroupWithTwoUsers(userId, memberId);

        try {
            response.put("status", mutualGroupId.isPresent() ? "ok" : "no mutual group found");
            response.put("groupId", mutualGroupId.isPresent() ? mutualGroupId.get().toString() : "");
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", "Invalid group ID format");
        }

        return response;
    }
}
