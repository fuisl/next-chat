package dev.nextchat.server.protocol.impl;

import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandContext;
import dev.nextchat.server.protocol.CommandType;
import dev.nextchat.server.auth.service.Authenticator;
import dev.nextchat.server.group.model.Group;
import dev.nextchat.server.group.service.GroupService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FetchGroupInfoCommand implements Command {
    private final String requestId;
    private final UUID groupId;
    private final GroupService groupService;
    private final Authenticator authenticator;

    public FetchGroupInfoCommand(String requestId, UUID groupId, GroupService groupService,
            Authenticator authenticator) {
        this.requestId = requestId;
        this.groupId = groupId;
        this.groupService = groupService;
        this.authenticator = authenticator;
    }

    @Override
    public CommandType getType() {
        return CommandType.FETCH_GROUP_INFO;
    }

    @Override
    public JSONObject execute(CommandContext context) {
        JSONObject response = new JSONObject();
        response.put("requestId", requestId);
        response.put("type", "fetch_group_info_response");

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

        Optional<Group> group = groupService.getGroupInfo(groupId);

        if (!group.isPresent()) {
            response.put("status", "error");
            response.put("message", "Group does not exist");
            return response;
        }

        List<UUID> groupMemberIds = groupService.getUserIdsInGroup(groupId);
        List<String> usernames = authenticator.getUsernameByUserIds(groupMemberIds);
        response.put("status", "ok");
        response.put("groupId", groupId.toString());
        response.put("name", group.get().getName());
        response.put("description", group.get().getDescription());
        response.put("members", new JSONArray(usernames));

        return response;
    }
}
