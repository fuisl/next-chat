package dev.nextchat.server.protocol.factory;

import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandType;
import dev.nextchat.server.protocol.impl.SignupCommand;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class SignupCommandFactory implements CommandFactory {

    @Override
    public CommandType getType() {
        return CommandType.SIGNUP;
    }

    @Override
    public Command create(JSONObject json) {
        String username = json.getString("username");
        String password = json.getString("passwd");
        return new SignupCommand(username, password);
    }
}
