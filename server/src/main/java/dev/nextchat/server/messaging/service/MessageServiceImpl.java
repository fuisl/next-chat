package dev.nextchat.server.messaging.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dev.nextchat.server.messaging.model.Message;
import dev.nextchat.server.messaging.repository.MessageRepository;

/**
 * Implementation of the {@link MessageService} interface.
 * Handles persistence and retrieval of chat messages.
 */
@Service
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;

    @Autowired
    public MessageServiceImpl(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public Message save(Message message) {
        return messageRepository.save(message);
    }

    @Override
    public List<Message> findRecentMessages(UUID groupId) {
        return messageRepository.findTop20ByGroupIdOrderByTimestampDesc(groupId);
    }

    @Override
    public List<Message> findMessagesBefore(UUID groupId, Instant before) {
        return messageRepository.findTop20ByGroupIdAndTimestampBeforeOrderByTimestampDesc(groupId, before);
    }

    @Override
    public List<Message> searchMessages(UUID groupId, String keyword) {
        return messageRepository.findByGroupIdAndContentRegex(groupId, keyword);
    }

    @Override
    public List<Message> findAllNewMessagesByGroups(List<UUID> groupIds, Instant after) {
        return messageRepository.findAllNewMessagesByGroups(groupIds, after);
    }
}
