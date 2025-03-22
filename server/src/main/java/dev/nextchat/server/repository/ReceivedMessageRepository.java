package dev.nextchat.server.repository;

import dev.nextchat.server.model.ReceivedMessage;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReceivedMessageRepository extends MongoRepository<ReceivedMessage, UUID> {

    // Find messages by sender
    // List<ReceivedMessage> findBySenderId(UUID senderId);

    // Find messages in a group
    List<ReceivedMessage> findByGroupId(UUID groupId);

    // Retrieve the most recent 20 messages based on groupId
    List<ReceivedMessage> findTop20ByGroupIdOrderByTimestampDesc(UUID groupId);

    List<ReceivedMessage> findTop20ByGroupIdAndTimestampBeforeOrderByTimestampDesc(UUID groupId, Instant timestamp);

    List<ReceivedMessage> findByGroupIdAndMessageRegex(UUID groupId, String searchText);
}
