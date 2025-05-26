package dev.nextchat.server.protocol.impl;

import dev.nextchat.server.auth.model.Credential;
import dev.nextchat.server.auth.service.Authenticator;
import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandContext;
import dev.nextchat.server.protocol.CommandType;

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
    public JSONObject execute(CommandContext context) {
        Authenticator authenticator = context.authenticator();
        JSONObject response = new JSONObject();
        Credential credential = new Credential(username, password);

        boolean success = authenticator.signUp(credential);

        response.put("type", "signup_response");
        response.put("status", success ? "ok" : "fail");
        response.put("message", success ? "Signup successful" : "Username already exists");

        if (success) {
            response.put("user", username);
        }

        return response;
    }
}
