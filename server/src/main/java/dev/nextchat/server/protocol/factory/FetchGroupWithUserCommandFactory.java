package dev.nextchat.server.protocol.factory;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandType;
import dev.nextchat.server.protocol.impl.FetchGroupWithUserCommand;

@Component
public class FetchGroupWithUserCommandFactory implements CommandFactory {

    @Override
    public CommandType getType() {
        return CommandType.FETCH_GROUP_WITH_USER;
    }

    @Override
    public Command create(JSONObject json) {
        String requestId = json.optString("requestId");
        String memberId = json.optString("memberId");
        return new FetchGroupWithUserCommand(requestId, memberId);
    }
}
