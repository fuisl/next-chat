package dev.nextchat.server.protocol.factory;

import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandType;
import dev.nextchat.server.protocol.impl.RenameGroupCommand;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class RenameGroupCommandFactory implements CommandFactory {

    @Override
    public Command create(JSONObject json) throws Exception {
        String requestId = json.optString("requestId");
        String groupId = json.optString("groupId");
        String name = json.optString("name");

        return new RenameGroupCommand(requestId, groupId, name);
    }

    @Override
    public CommandType getType() {
        return CommandType.RENAME_GROUP;
    }
}
