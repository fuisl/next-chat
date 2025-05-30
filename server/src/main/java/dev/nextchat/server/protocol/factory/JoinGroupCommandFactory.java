package dev.nextchat.server.protocol.factory;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandType;
import dev.nextchat.server.protocol.impl.JoinGroupCommand;

@Component
public class JoinGroupCommandFactory implements CommandFactory {

    @Override
    public CommandType getType() {
        return CommandType.JOIN_GROUP;
    }

    @Override
    public Command create(JSONObject json) {
        String groupId = json.getString("groupId");
        String userId = json.optString("userId", null);
        return new JoinGroupCommand(groupId, userId);
    }
}
