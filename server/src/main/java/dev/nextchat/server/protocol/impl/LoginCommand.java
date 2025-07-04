package dev.nextchat.server.protocol.impl;

import dev.nextchat.server.auth.model.Credential;
import dev.nextchat.server.auth.service.Authenticator;
import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandContext;
import dev.nextchat.server.protocol.CommandType;
import dev.nextchat.server.session.service.SessionService;
import dev.nextchat.server.shared.dto.SessionToken;
import org.json.JSONObject;

import java.util.UUID;

public class LoginCommand implements Command {

    private final String username;
    private final String password;
    private SessionToken sessionToken;

    public SessionToken getSessionToken() {
        return sessionToken;
    }

    public LoginCommand(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public CommandType getType() {
        return CommandType.LOGIN;
    }

    @Override
    public JSONObject execute(CommandContext context) {
        Authenticator authenticator = context.authenticator();
        SessionService sessionService = context.sessionService();

        JSONObject response = new JSONObject();
        Credential credential = new Credential(username, password);

        boolean success = authenticator.signIn(credential);

        response.put("type", "login_response");
        response.put("status", success ? "ok" : "fail");

        if (success) {
            UUID userId = authenticator.getUserIdByUsername(username);  // Make sure this method exists
            String sessionTokenStr = generateToken();
            sessionService.registerSession(sessionTokenStr, userId);
            
            this.sessionToken = new SessionToken(sessionTokenStr, userId);

            response.put("token", this.sessionToken.getToken());
            response.put("userId", this.sessionToken.getUserId().toString());
            response.put("message", "Login successful");
        } else {
            response.put("message", "Invalid username or password");
        }

        return response;
    }

    private String generateToken() {
        return UUID.randomUUID().toString();  // TODO: implement JWT or other secure token generation
    }
}
