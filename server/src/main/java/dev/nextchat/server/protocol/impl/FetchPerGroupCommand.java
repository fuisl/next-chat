package dev.nextchat.server.protocol.impl;

import dev.nextchat.server.messaging.model.Message;
import dev.nextchat.server.messaging.service.MessageService;
import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandContext;
import dev.nextchat.server.protocol.CommandType;
import dev.nextchat.server.group.model.Group;
import dev.nextchat.server.group.service.GroupService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.Instant;

public class FetchPerGroupCommand implements Command {
    private final Instant timestamp;
    private final String requestId;
    private final MessageService messageService;

    public FetchPerGroupCommand(String requestId, Instant timestamp, MessageService messageService) {
        this.timestamp = timestamp;
        this.requestId = requestId;
        this.messageService = messageService;
    }

    @Override
    public CommandType getType() {
        return CommandType.FETCH_PER_GROUP;
    }

    @Override
    public JSONObject execute(CommandContext context) {
        GroupService groupService = context.groupService();

        JSONObject response = new JSONObject();
        response.put("requestId", requestId);

        if (!context.isAuthenticated()) {
            response.put("status", "error");
            response.put("message", "Authentication required");
            return response;
        }

        UUID userId = context.sessionUserId();
        List<Group> groups = groupService.getGroupsForUser(userId);
        List<UUID> groupIds = new ArrayList<>();

        if (groups.size() != 0) {
            groupIds = groups.stream()
                    .map(Group::getId)
                    .collect(Collectors.toList());
        } else {
            response.put("status", "error");
            response.put("message", "User is not in any group!");
            return response;
        }

        List<Message> messages = messageService.findLatestMessagePerGroup(groupIds, timestamp);

        JSONArray jsonMessages = new JSONArray();
        for (Message m : messages) {
            JSONObject msg = new JSONObject();
            msg.put("id", m.getId().toString());
            msg.put("senderId", m.getSenderId().toString());
            msg.put("senderUsername", m.getSenderUsername());
            msg.put("groupId", m.getGroupId().toString());
            msg.put("content", m.getContent());
            msg.put("timestamp", m.getTimestamp().toString());
            jsonMessages.put(msg);
        }

        response.put("status", "ok");
        response.put("messages", jsonMessages);
        return response;
    }
}
