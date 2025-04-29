package dev.nextchat.server.protocol.factory;

import dev.nextchat.server.messaging.service.MessageService;
import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandType;
import dev.nextchat.server.protocol.impl.EditMessageCommand;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EditMessageCommandFactory implements CommandFactory {

    private final MessageService messageService;

    public EditMessageCommandFactory(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public Command create(JSONObject json) throws Exception {
        UUID messageId = UUID.fromString(json.getString("messageId"));
        String newContent = json.getString("newContent");
        return new EditMessageCommand(messageId, newContent, messageService);
    }

    @Override
    public CommandType getType() {
        return CommandType.EDIT_MESSAGE;
    }
}
