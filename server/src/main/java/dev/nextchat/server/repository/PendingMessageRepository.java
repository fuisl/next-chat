package dev.nextchat.server.repository;

import dev.nextchat.server.model.PendingMessage;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PendingMessageRepository extends MongoRepository<PendingMessage, UUID> {

    // Find messages by sender
    List<PendingMessage> findBySenderId(UUID senderId);

    // Find messages in a group
    List<PendingMessage> findByGroupId(UUID groupId);
}
