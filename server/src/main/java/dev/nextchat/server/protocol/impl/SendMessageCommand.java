package dev.nextchat.server.protocol.impl;

import dev.nextchat.server.messaging.handler.SendMessageHandler;
import dev.nextchat.server.messaging.model.Message;
import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandContext;
import dev.nextchat.server.protocol.CommandType;

import org.json.JSONObject;

import java.util.UUID;

public class SendMessageCommand implements Command {

    private final String content;
    private final UUID groupId;
    private final SendMessageHandler handler;

    public SendMessageCommand(String content, String groupId, SendMessageHandler handler) {
        this.content = content;
        this.groupId = UUID.fromString(groupId);
        this.handler = handler;
    }

    @Override
    public CommandType getType() {
        return CommandType.SEND_MESSAGE;
    }

    @Override
    public JSONObject execute(CommandContext context) {
        JSONObject response = new JSONObject();
        response.put("type", "send_message_response");

        try {
            Message message = handler.handle(groupId, content, context);

            response.put("status", "ok");
            response.put("messageId", message.getId().toString());
            response.put("groupId", message.getGroupId().toString());
            response.put("senderId", message.getSenderId().toString());
            response.put("timestamp", message.getTimestamp().toString());
            response.put("content", message.getContent());

        } catch (IllegalStateException | IllegalArgumentException e) {
            response.put("status", "error");
            response.put("error", e.getMessage());
        }

        return response;
    }
}
