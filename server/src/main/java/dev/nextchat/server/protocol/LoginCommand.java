package dev.nextchat.server.protocol;

import org.json.JSONObject;

public class LoginCommand implements Command {
    private final String username;
    private final String password;

    public LoginCommand(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public CommandType getType() {
        return CommandType.LOGIN;
    }

    @Override
    public JSONObject execute() {
        // Placeholder response
        JSONObject response = new JSONObject();
        response.put("type", "login_response");
        response.put("status", "ok");
        response.put("message", "Login command received");
        response.put("user", username);
        return response;
    }
}
