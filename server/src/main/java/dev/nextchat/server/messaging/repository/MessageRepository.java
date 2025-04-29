package dev.nextchat.server.messaging.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import dev.nextchat.server.messaging.model.Message;

public interface MessageRepository extends MongoRepository<Message, UUID> {

    List<Message> findByGroupId(UUID groupId);

    List<Message> findTop20ByGroupIdOrderByTimestampDesc(UUID groupId);

    List<Message> findTop20ByGroupIdAndTimestampBeforeOrderByTimestampDesc(UUID groupId, Instant timestamp);

    List<Message> findByGroupIdAndContentRegex(UUID groupId, String searchText);

    Message deleteById(UUID messageId);
}
