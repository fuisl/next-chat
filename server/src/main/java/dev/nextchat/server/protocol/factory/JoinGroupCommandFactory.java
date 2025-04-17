package dev.nextchat.server.protocol.factory;

import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandType;
import dev.nextchat.server.protocol.impl.JoinGroupCommand;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class JoinGroupCommandFactory implements CommandFactory {

    @Override
    public CommandType getType() {
        return CommandType.JOIN_GROUP;
    }

    @Override
    public Command create(JSONObject json) {
        String groupId = json.getString("groupId");
        return new JoinGroupCommand(groupId);
    }
}
