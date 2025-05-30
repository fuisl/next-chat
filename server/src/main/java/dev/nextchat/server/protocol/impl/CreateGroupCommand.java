package dev.nextchat.server.protocol.impl;

import dev.nextchat.server.group.model.Group;
import dev.nextchat.server.group.service.GroupService;
import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandContext;
import dev.nextchat.server.protocol.CommandType;

import org.json.JSONObject;

import java.util.UUID;

public class CreateGroupCommand implements Command {

    private final String name;
    private final String description;

    public CreateGroupCommand(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public CommandType getType() {
        return CommandType.CREATE_GROUP;
    }

    @Override
    public JSONObject execute(CommandContext context) {
        if (!context.isAuthenticated()) {
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("type", "create_group_response");
            errorResponse.put("status", "error");
            errorResponse.put("message", "User is not authenticated");
            return errorResponse;
        }

        GroupService groupService = context.groupService();
        UUID userId = context.sessionUserId();

        Group newGroup = groupService.createGroup(name, description, userId);

        JSONObject response = new JSONObject();
        response.put("type", "create_group_response");
        response.put("status", "ok");
        response.put("groupId", newGroup.getId().toString());
        response.put("message", "Group created successfully");

        return response;
    }
}
