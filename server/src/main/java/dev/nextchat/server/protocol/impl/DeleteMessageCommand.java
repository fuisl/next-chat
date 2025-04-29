package dev.nextchat.server.protocol.impl;

import dev.nextchat.server.messaging.model.Message;
import dev.nextchat.server.messaging.service.MessageService;
import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandContext;
import dev.nextchat.server.protocol.CommandType;

import org.json.JSONObject;

import java.util.UUID;

public class DeleteMessageCommand implements Command {

    private final UUID messageId;
    private final MessageService messageService;

    public DeleteMessageCommand(UUID messageId, MessageService messageService) {
        this.messageId = messageId;
        this.messageService = messageService;
    }

    @Override
    public CommandType getType() {
        return CommandType.DELETE_MESSAGE;
    }

    @Override
    public JSONObject execute(CommandContext context) {
        JSONObject response = new JSONObject();
        response.put("type", "delete_message_response");

        if (!context.isAuthenticated()) {
            response.put("status", "error");
            response.put("message", "Authentication required");
            return response;
        }

        UUID userId = context.sessionUserId();
        Message message = messageService.findById(messageId);

        if (message == null) {
            response.put("status", "error");
            response.put("message", "Message not found");
            return response;
        }

        if (!message.getSenderId().equals(userId)) {
            response.put("status", "error");
            response.put("message", "You can only delete your own messages");
            return response;
        }

        messageService.deleteMessage(messageId);

        response.put("status", "ok");
        response.put("messageId", messageId.toString());
        return response;
    }
}
