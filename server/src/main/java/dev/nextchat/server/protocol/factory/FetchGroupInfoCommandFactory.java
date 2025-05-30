package dev.nextchat.server.protocol.factory;

import dev.nextchat.server.auth.service.Authenticator;
import dev.nextchat.server.group.service.GroupService;
import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandType;
import dev.nextchat.server.protocol.impl.FetchGroupInfoCommand;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FetchGroupInfoCommandFactory implements CommandFactory {

    private final GroupService groupService;
    private final Authenticator authenticator;

    public FetchGroupInfoCommandFactory(GroupService groupService, Authenticator authenticator) {
        this.groupService = groupService;
        this.authenticator = authenticator;
    }

    @Override
    public Command create(JSONObject json) throws Exception {
        String requestId = json.optString("requestId");
        UUID groupId = UUID.fromString(json.getString("groupId"));

        return new FetchGroupInfoCommand(requestId, groupId, groupService, authenticator);
    }

    @Override
    public CommandType getType() {
        return CommandType.FETCH_GROUP_INFO;
    }
}
