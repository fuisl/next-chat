package dev.nextchat.server.protocol.impl;

import dev.nextchat.server.messaging.model.Message;
import dev.nextchat.server.messaging.service.MessageService;
import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandContext;
import dev.nextchat.server.protocol.CommandType;
import dev.nextchat.server.group.service.GroupService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID; 

import java.time.Instant;

public class FetchBeforeMessageCommand implements Command {
    private final UUID groupId;
    private final MessageService messageService;
    private final GroupService groupService;
    private final Instant timestamp;

    public FetchBeforeMessageCommand(UUID groupId, MessageService messageService, GroupService groupService, Instant timestamp) {
        this.groupId = groupId;
        this.messageService = messageService;
        this.groupService = groupService;
        this.timestamp = timestamp;
    }

    @Override
    public CommandType getType() {
        return CommandType.FETCH_BEFORE;
    }

    @Override
    public JSONObject execute(CommandContext context) {
        JSONObject response = new JSONObject();
        response.put("type", "fetch_message_response");

        if (!context.isAuthenticated()) {
            response.put("status", "error");
            response.put("message", "Authentication required");
            return response;
        }

        UUID userId = context.sessionUserId();

        if (!groupService.isUserInGroup(groupId, userId)) {
            response.put("status", "error");
            response.put("message", "User is not a member of the group");
            return response;
        }

        List<Message> messages = messageService.findMessagesBefore(groupId, timestamp);

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
