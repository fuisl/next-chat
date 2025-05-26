package dev.nextchat.server.protocol.factory;

import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandType;
import dev.nextchat.server.protocol.impl.CheckUserExistCommand;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class CheckUserExistCommandFactory implements CommandFactory {

    @Override
    public Command create(JSONObject json) {
        String username = json.getString("username");
        return new CheckUserExistCommand(username);
    }

    @Override
    public CommandType getType() {
        return CommandType.CHECK_USER_EXISTENCE;
    }
}

