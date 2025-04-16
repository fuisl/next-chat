package dev.nextchat.server.protocol;

import org.json.JSONObject;

public class SignupCommand implements Command {
    private final String username;
    private final String password;

    public SignupCommand(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public CommandType getType() {
        return CommandType.SIGNUP;
    }

    @Override
    public JSONObject execute() {
        JSONObject response = new JSONObject();
        response.put("type", "signup_response");
        response.put("status", "ok");
        response.put("message", "Signup command received");
        response.put("user", username);
        return response;
    }
}
