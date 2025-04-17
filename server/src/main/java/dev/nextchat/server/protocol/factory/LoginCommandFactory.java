package dev.nextchat.server.protocol.factory;

import dev.nextchat.server.protocol.impl.LoginCommand;
import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandType;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class LoginCommandFactory implements CommandFactory {
    @Override
    public Command create(JSONObject json) throws Exception {
        String username = json.getString("username");
        String password = json.getString("passwd");
        return new LoginCommand(username, password);
    }

    @Override
    public CommandType getType() {
        return CommandType.LOGIN;
    }
}
