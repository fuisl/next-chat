package dev.nextchat.server.protocol.factory;

import dev.nextchat.server.messaging.service.MessageService;
import dev.nextchat.server.group.service.GroupService;
import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandType;
import dev.nextchat.server.protocol.impl.FetchNewMessageCommand;
import dev.nextchat.server.auth.service.Authenticator;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class FetchNewMessageCommandFactory implements CommandFactory {

    private final MessageService messageService;
    private final GroupService groupService;
    private final Authenticator authenticator;

    public FetchNewMessageCommandFactory(MessageService messageService, GroupService groupService,
            Authenticator authenticator) {
        this.messageService = messageService;
        this.groupService = groupService;
        this.authenticator = authenticator;
    }

    @Override
    public Command create(JSONObject json) throws Exception {
        return new FetchNewMessageCommand(messageService, groupService, authenticator);
    }

    @Override
    public CommandType getType() {
        return CommandType.FETCH_NEW;
    }
}
