package dev.nextchat.server.messaging.handler;

import dev.nextchat.server.group.service.GroupService;
import dev.nextchat.server.messaging.model.Message;
import dev.nextchat.server.messaging.service.MessageService;
import dev.nextchat.server.protocol.CommandContext;
import dev.nextchat.server.session.model.Session;
import dev.nextchat.server.messaging.service.RelayService;

import java.util.List;
import java.util.UUID;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class SendMessageHandler {

    private final MessageService messageService;
    private final GroupService groupService;
    private final RelayService relayService;

    public SendMessageHandler(MessageService messageService, GroupService groupService, RelayService relayService) {
        this.messageService = messageService;
        this.groupService = groupService;
        this.relayService = relayService;
    }

    public Message handle(UUID groupId, String content, CommandContext context) {
        // 🔐 Validate session and fetch sender
        Session session = context.sessionService()
            .getSession(context.sessionUserId())
            .orElseThrow(() -> new IllegalStateException("Session not found for user"));

        UUID senderId = session.getUserId();

        if (!groupService.isUserInGroup(groupId, senderId)) {
            throw new IllegalArgumentException("User " + senderId + " is not a member of group " + groupId);
        }

        // 💾 Save message to DB
        Message message = messageService.save(new Message(groupId, senderId, content));

        // 📡 Relay to online group members (except sender)
        List<UUID> groupMembers = groupService.getUserIdsInGroup(groupId);
        JSONObject json = new JSONObject();
        json.put("type", "message");
        json.put("groupId", groupId.toString());
        json.put("senderId", senderId.toString());
        json.put("content", content);
        json.put("timestamp", message.getTimestamp().toString());

        for (UUID memberId : groupMembers) {
            if (!memberId.equals(senderId)) {
                relayService.sendToUser(memberId, json.toString());
            }
        }

        return message;
    }
}
