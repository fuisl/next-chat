package dev.nextchat.server.protocol.impl;

import dev.nextchat.server.auth.model.User;
import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandContext;
import dev.nextchat.server.protocol.CommandType;
import org.json.JSONObject;

import java.util.Optional;

public class CheckUserExistCommand implements Command {
    private final String username;

    public CheckUserExistCommand(String username) {
        this.username = username;
    }

    @Override
    public CommandType getType() {
        return CommandType.CHECK_USER_EXISTENCE;
    }

    @Override
    public JSONObject execute(CommandContext context) {
        JSONObject response = new JSONObject();
        response.put("type", "checkUserExistenceResponse");
        response.put("username", username);
        Optional<User> userOpt = context.getUserRepository().findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            response.put("exists", true);
            response.put("userId", user.getId().toString());
        } else {
            response.put("exists", false);
        }

        return response;
    }
}
