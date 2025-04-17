package dev.nextchat.server.messaging.handler;

import dev.nextchat.server.group.service.GroupService;
import dev.nextchat.server.messaging.model.Message;
import dev.nextchat.server.messaging.service.MessageService;
import dev.nextchat.server.protocol.CommandContext;
import dev.nextchat.server.session.model.Session;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SendMessageHandler {

    private final MessageService messageService;
    private final GroupService groupService;

    public SendMessageHandler(MessageService messageService, GroupService groupService) {
        this.messageService = messageService;
        this.groupService = groupService;
    }

    public Message handle(UUID groupId, String content, CommandContext context) {
        Session session = context.sessionService()
            .getSession(context.sessionUserId())
            .orElseThrow(() -> new IllegalStateException("Session not found for user"));

        UUID senderId = session.getUserId();

        if (!groupService.isUserInGroup(groupId, senderId)) {
            throw new IllegalArgumentException("User " + senderId + " is not a member of group " + groupId);
        }

        return messageService.save(new Message(groupId, senderId, content));
    }
}
