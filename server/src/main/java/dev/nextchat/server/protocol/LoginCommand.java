package dev.nextchat.server.protocol;

import dev.nextchat.server.auth.model.Credential;
import dev.nextchat.server.auth.service.Authenticator;
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
    public JSONObject execute(Authenticator authenticator) {
        JSONObject response = new JSONObject();
        Credential credential = new Credential(username, password);

        boolean success = authenticator.signIn(credential);

        response.put("type", "login_response");
        response.put("status", success ? "ok" : "fail");
        response.put("message", success ? "Login successful" : "Invalid username or password");

        if (success) {
            response.put("user", username);
        }

        return response;
    }
}