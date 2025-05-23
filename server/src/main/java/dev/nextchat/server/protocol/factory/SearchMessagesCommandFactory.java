package dev.nextchat.server.protocol.factory;

import dev.nextchat.server.messaging.service.MessageService;
import dev.nextchat.server.group.service.GroupService;
import dev.nextchat.server.protocol.Command;
import dev.nextchat.server.protocol.CommandType;
import dev.nextchat.server.protocol.impl.SearchMessagesCommand;

import java.util.UUID;

import org.json.JSONObject;
import org.springframework.stereotype.Component;


@Component
public class SearchMessagesCommandFactory implements CommandFactory {
    private final MessageService messageService;
    private final GroupService groupService;

    public SearchMessagesCommandFactory(MessageService messageService, GroupService groupService) {
        this.messageService = messageService;
        this.groupService = groupService;
    }

    @Override
    public Command create(JSONObject json) throws Exception {
        UUID groupId = UUID.fromString(json.getString("groupId"));
        String keyword = json.getString("keyword");
        return new SearchMessagesCommand(groupId, keyword, messageService, groupService);
    }

    @Override
    public CommandType getType() {
        return CommandType.SEARCH_MESSAGES;
    }
    
}
