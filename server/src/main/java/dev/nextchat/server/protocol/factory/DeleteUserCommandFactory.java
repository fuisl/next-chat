package dev.nextchat.server.protocol.factory;

import dev.nextchat.server.messaging.service.MessageService;
import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandType;
import dev.nextchat.server.protocol.impl.DeleteUserCommand;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class DeleteUserCommandFactory implements CommandFactory {

    private final MessageService messageService;

    public DeleteUserCommandFactory(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public Command create(JSONObject json) throws Exception {
        String requestId = json.optString("requestId");

        return new DeleteUserCommand(requestId, messageService);
    }

    @Override
    public CommandType getType() {
        return CommandType.DELETE_USER;
    }
}
