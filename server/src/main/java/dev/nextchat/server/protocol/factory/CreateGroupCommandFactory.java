package dev.nextchat.server.protocol.factory;

import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandType;
import dev.nextchat.server.protocol.impl.CreateGroupCommand;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class CreateGroupCommandFactory implements CommandFactory {

    @Override
    public CommandType getType() {
        return CommandType.CREATE_GROUP;
    }

    @Override
    public Command create(JSONObject json) {
        String name = json.getString("name");
        String description = json.optString("description", ""); // default empty string if not provided
        return new CreateGroupCommand(name, description);
    }
}
