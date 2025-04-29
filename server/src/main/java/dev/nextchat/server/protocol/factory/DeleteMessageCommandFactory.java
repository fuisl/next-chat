package dev.nextchat.server.protocol.factory;

import dev.nextchat.server.messaging.service.MessageService;
import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandType;
import dev.nextchat.server.protocol.impl.DeleteMessageCommand;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DeleteMessageCommandFactory implements CommandFactory {

    private final MessageService messageService;

    public DeleteMessageCommandFactory(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public Command create(JSONObject json) throws Exception {
        UUID messageId = UUID.fromString(json.getString("messageId"));
        return new DeleteMessageCommand(messageId, messageService);
    }

    @Override
    public CommandType getType() {
        return CommandType.DELETE_MESSAGE;
    }
}
