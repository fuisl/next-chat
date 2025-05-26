package dev.nextchat.server.messaging.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import dev.nextchat.server.messaging.model.Message;

/**
 * Service interface for handling messaging operations within groups.
 */
public interface MessageService {

    /**
     * Saves a new message to the database.
     *
     * @param message the message object containing groupId, senderId, content, and
     *                timestamp
     * @return the persisted message
     */
    Message save(Message message);

    /**
     * Retrieves the most recent 20 messages for a specific group.
     *
     * @param groupId the group ID
     * @return a list of messages sorted by timestamp descending
     */
    List<Message> findRecentMessages(UUID groupId);

    /**
     * Retrieves up to 20 older messages for a group sent before the given
     * timestamp.
     *
     * @param before timestamp cutoff (exclusive)
     * @return list of messages older than the given timestamp
     */
    List<Message> findMessagesBefore(UUID groupId, Instant before);

    List<Message> findAllNewMessagesByGroups(List<UUID> groupIds, Instant timestamp);

    List<Message> findLatestMessagePerGroup(List<UUID> groupIds, Instant timestamp);

    void changeDeletedUserSenderName(UUID userId);

    /**
     * Searches messages in a group using a case-insensitive regex keyword.
     *
     * @param groupId the group ID
     * @param keyword the regex keyword to match
     * @return list of matched messages
     */
    List<Message> searchMessages(UUID groupId, String keyword);
}
