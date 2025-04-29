package dev.nextchat.server.protocol.impl;

import dev.nextchat.server.group.service.GroupService;
import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandContext;
import dev.nextchat.server.protocol.CommandType;
import dev.nextchat.server.session.service.SessionService;
import dev.nextchat.server.messaging.service.RelayService;

import org.json.JSONObject;

import java.util.List;
import java.util.UUID;

public class TypingIndicatorCommand implements Command {

    private final UUID groupId;
    private final boolean isTyping;
    private final GroupService groupService;
    private final RelayService relayService;

    public TypingIndicatorCommand(UUID groupId, boolean isTyping, GroupService groupService, RelayService relayService) {
        this.groupId = groupId;
        this.isTyping = isTyping;
        this.groupService = groupService;
        this.relayService = relayService;
    }

    @Override
    public CommandType getType() {
        return CommandType.TYPING_INDICATOR;
    }

    @Override
    public JSONObject execute(CommandContext context) {
        JSONObject response = new JSONObject();
        response.put("type", "typing_indicator");

        if (!context.isAuthenticated()) {
            response.put("status", "error");
            response.put("message", "Authentication required");
            return response;
        }

        UUID userId = context.sessionUserId();

        if (!groupService.isUserInGroup(groupId, userId)) {
            response.put("status", "error");
            response.put("message", "You are not in this group");
            return response;
        }

        // Broadcast to all users in the group (excluding sender optionally)
        List<UUID> groupMembers = groupService.getUserIdsInGroup(groupId);

        JSONObject response = new JSONObject();
        response.put("type", "typing_indicator");
        response.put("groupId", groupId.toString());
        response.put("userId", userId.toString());
        response.put("isTyping", isTyping);

        for (UUID memberId : groupMembers) {
            if (!memberId.equals(userId)) {
                relayService.sendToUser(memberId, response.toString());
            }
        }
        return response;
    }
}
