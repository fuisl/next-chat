package dev.nextchat.server.protocol.impl;

import dev.nextchat.server.auth.model.User;
import dev.nextchat.server.auth.service.Authenticator;
import dev.nextchat.server.messaging.service.MessageService;
import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandContext;
import dev.nextchat.server.protocol.CommandType;
import org.json.JSONObject;

import java.util.Optional;
import java.util.UUID;

public class DeleteUserCommand implements Command {

    private final String requestId;
    private final MessageService messageService;

    public DeleteUserCommand(String requestId, MessageService messageService) {
        this.requestId = requestId;
        this.messageService = messageService;
    }

    @Override
    public CommandType getType() {
        return CommandType.DELETE_USER;
    }

    @Override
    public JSONObject execute(CommandContext context) {
        Authenticator authenticator = context.authenticator();
        JSONObject response = new JSONObject();
        response.put("requestId", requestId);
        response.put("type", "delete_user_response");

        if (!context.isAuthenticated()) {
            response.put("status", "error");
            response.put("message", "Authentication required");
            return response;
        }

        UUID userId = context.sessionUserId();
        response.put("userId", userId);

        Optional<User> user = authenticator.getUserByUserId(userId);
        if (user.isEmpty()) {
            response.put("status", "error");
            response.put("message", "Invalid user id");
            return response;
        }

        int status = authenticator.deleteUser(userId);

        if (status != 1) {
            response.put("status", "error");
            response.put("message", "Something went wrong?");
            return response;
        }

        messageService.changeDeletedUserSenderName(userId);

        response.put("status", "ok");
        response.put("message", "User deleted!");
        return response;
    }
}
