package dev.nextchat.server.messaging.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import dev.nextchat.server.messaging.model.Message;

public interface CustomMessageRepository {
    List<Message> findLatestMessagesPerGroupBeforeTimestamp(List<UUID> groupIds, Instant beforeTime);
}
