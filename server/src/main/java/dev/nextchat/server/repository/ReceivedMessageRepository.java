package dev.nextchat.server.repository;

import dev.nextchat.server.model.ReceivedMessage;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Interface repository for working with the {@code received_message}
 * collection.
 */
public interface ReceivedMessageRepository extends MongoRepository<ReceivedMessage, UUID> {

    // Find messages by sender
    // List<ReceivedMessage> findBySenderId(UUID senderId);

    // Find messages in a group
    List<ReceivedMessage> findByGroupId(UUID groupId);

    /**
     * Returns documents matching a given {@code groupId}. The results are
     * sorted by {@code timestamp} by most recent and limit to an N number of 
     * docs returned. Currently N=20.
     * @param groupId must be an existing {@code groupId} of a conversation
     * @param timestamp must be an {@code Instant} instance, the conversion
     * should be handled correctly by any service using this repository.
     * @return a list of {@code ReceivedMessage}. @see ReceivedMessage.
     * 
     * This method is currently conceived as REDUNDANT by the author. 
     * @see #findTop20ByGroupIdOrderByTimestampDesc(UUID groupId)
     */
    List<ReceivedMessage> findTop20ByGroupIdOrderByTimestampDesc(UUID groupId);

    /**
     * Returns documents matching a given {@code groupId} and before a given 
     * {@code timestamp}. The results are sorted by {@code timestamp} by most
     * recent and limit to an N number of docs returned. Currently N=20.
     * @param groupId must be an existing {@code groupId} of a conversation
     * @param timestamp must be an {@code Instant} instance, the conversion
     * should be handled correctly by any service using this repository.
     * @return a list of {@code ReceivedMessage}. 
     * @see dev.nextchat.server.model.ReceivedMessage.
     */
    List<ReceivedMessage> findTop20ByGroupIdAndTimestampBeforeOrderByTimestampDesc(UUID groupId, Instant timestamp);

    /**
     * Returns documents matching a given {@code groupId} and a {@code searchText}
     * regex pattern.
     * @param groupId must be an existing {@code groupId} of a conversation
     * @param searchText a regex pattern, projected to be used as simple
     * whole-word searching method. Ex: searching "are" would match "How ARE you?"
     * but not "A fuRthEr route".
     * @return a list of {@code ReceivedMessage}. @see ReceivedMessage.
     */
    List<ReceivedMessage> findByGroupIdAndMessageRegex(UUID groupId, String searchText);
}
