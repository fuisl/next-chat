package dev.nextchat.server.protocol.impl;

import dev.nextchat.server.messaging.model.Message;
import dev.nextchat.server.messaging.service.MessageService;
import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandContext;
import dev.nextchat.server.protocol.CommandType;
import dev.nextchat.server.auth.model.User;
import dev.nextchat.server.group.model.Group;
import dev.nextchat.server.group.service.GroupService;
import dev.nextchat.server.auth.service.Authenticator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

import java.time.Instant;
import java.util.stream.Collectors;

public class FetchNewMessageCommand implements Command {
    private final MessageService messageService;
    private final GroupService groupService;
    private final Authenticator authenticator;

    public FetchNewMessageCommand(MessageService messageService, GroupService groupService,
            Authenticator authenticator) {
        this.messageService = messageService;
        this.groupService = groupService;
        this.authenticator = authenticator;
    }

    @Override
    public CommandType getType() {
        return CommandType.FETCH_NEW;
    }

    @Override
    public JSONObject execute(CommandContext context) {
        JSONObject response = new JSONObject();
        response.put("type", "fetch_new_message_response");

        if (!context.isAuthenticated()) {
            response.put("status", "error");
            response.put("message", "Authentication required");
            return response;
        }

        UUID userId = context.sessionUserId();
        Optional<User> opt = authenticator.getUserByUserId(userId);
        List<Group> groups = groupService.getGroupsForUser(userId);
        Instant lastOnlineTimeStamp;
        List<UUID> groupIds;

        if (groups.size() != 0) {
            groupIds = groups.stream()
                    .map(Group::getId)
                    .collect(Collectors.toList());
        } else {
            response.put("status", "error");
            response.put("message", "User is not in any group!");
            return response;
        }

        if (opt.isPresent()) {
            lastOnlineTimeStamp = opt.get().getLastOnlineTimeStamp() != null ? opt.get().getLastOnlineTimeStamp()
                    : opt.get().getCreateTimeStamp();
        } else {
            response.put("status", "error");
            response.put("message", "User does not exist!");
            return response;
        }

        List<Message> messages = messageService.findAllNewMessagesByGroups(groupIds,
                lastOnlineTimeStamp);

        JSONArray jsonMessages = new JSONArray();
        for (Message m : messages) {
            JSONObject msg = new JSONObject();
            msg.put("id", m.getId().toString());
            msg.put("senderId", m.getSenderId().toString());
            msg.put("senderUsername", m.getSenderUsername());
            msg.put("content", m.getContent());
            msg.put("timestamp", m.getTimestamp().toString());
            jsonMessages.put(msg);
        }

        response.put("status", "ok");
        response.put("messages", jsonMessages);
        return response;
    }
}
