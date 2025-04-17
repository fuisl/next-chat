package dev.nextchat.server.messaging;

import dev.nextchat.server.messaging.model.Message;
import dev.nextchat.server.messaging.repository.MessageRepository;
import dev.nextchat.server.messaging.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MessageServiceTest {

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageRepository messageRepository;

    private UUID groupId;
    private UUID senderId;

    @BeforeEach
    public void setUp() {
        messageRepository.deleteAll();
        groupId = UUID.randomUUID();
        senderId = UUID.randomUUID();
    }

    @Test
    public void testSaveAndFindRecentMessages() {
        Message msg = new Message(groupId, senderId, "Hello world", Instant.now());
        messageService.save(msg);

        List<Message> recentMessages = messageService.findRecentMessages(groupId);
        assertEquals(1, recentMessages.size());
        assertEquals("Hello world", recentMessages.get(0).getContent());
    }

    @Test
    public void testFindMessagesBefore() throws InterruptedException {
        Instant now = Instant.now();
        Thread.sleep(5); // ensure timestamp difference

        messageService.save(new Message(groupId, senderId, "Old message", now));
        messageService.save(new Message(groupId, senderId, "New message", Instant.now()));

        List<Message> messages = messageService.findMessagesBefore(groupId, Instant.now().minusMillis(1));
        assertEquals(1, messages.size());
        assertEquals("Old message", messages.get(0).getContent());
    }

    @Test
    public void testSearchMessages() {
        messageService.save(new Message(groupId, senderId, "Hello there", Instant.now()));
        messageService.save(new Message(groupId, senderId, "Something else", Instant.now()));

        List<Message> results = messageService.searchMessages(groupId, "Hello");
        assertEquals(1, results.size());
        assertTrue(results.get(0).getContent().contains("Hello"));
    }

    @Test
    public void testEmptyWhenGroupDoesNotMatch() {
        UUID otherGroup = UUID.randomUUID();
        messageService.save(new Message(groupId, senderId, "Hello", Instant.now()));

        List<Message> result = messageService.findRecentMessages(otherGroup);
        assertTrue(result.isEmpty());
    }
}
