package dev.nextchat.server.protocol.impl;

import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import dev.nextchat.server.auth.model.User;
import dev.nextchat.server.auth.service.Authenticator;
import dev.nextchat.server.group.model.Group;
import dev.nextchat.server.group.service.GroupService;
import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandContext;
import dev.nextchat.server.protocol.CommandType;

public class SearchConversationCommand implements Command {
    private final String requestId;
    private final String search;

    public SearchConversationCommand(String requestId, String search) {
        this.requestId = requestId;
        this.search = search;
    }

    @Override
    public CommandType getType() {
        return CommandType.SEARCH_CONVERSATION;
    }

    @Override
    public JSONObject execute(CommandContext context) {
        Authenticator authenticator = context.authenticator();
        GroupService groupService = context.groupService();
        UUID userId = context.getSession().getUserId();

        JSONObject response = new JSONObject();

        if (!context.isAuthenticated()) {
            response.put("type", "join_group_response");
            response.put("status", "error");
            response.put("message", "User is not authenticated");
            return response;
        }

        JSONArray res = new JSONArray();
        List<User> users = authenticator.getUserByPattern(search);

        for (User user : users) {
            if (!user.getId().equals(userId)) {
                JSONObject msg = new JSONObject();
                msg.put("type", "user");
                msg.put("id", user.getId().toString());
                msg.put("name", user.getUsername());
                res.put(msg);
            }
        }

        List<Group> groups = groupService.getGroupsByPattern(search);

        for (Group group : groups) {
            JSONObject msg = new JSONObject();
            msg.put("type", "group");
            msg.put("id", group.getId().toString());
            msg.put("name", group.getName());
            msg.put("description", group.getDescription());
            res.put(msg);
        }
        response.put("type", "search_conversation_response");
        response.put("requestId", requestId);
        response.put("status", "ok");
        response.put("matched_count", res.length());
        response.put("results", res);

        return response;
    }
}
