package dev.nextchat.server.model;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;

/**
 * Abstract class for {@code ReceivedMessage} and {@code PendingMessage}.
 * This class only provides some of the important attributes except the
 * {@code senderId/receiverId}, which depends on the collection the model
 * working with.
 */
public abstract class Message {

    @Id
    protected UUID id;
    protected UUID groupId;
    protected String message;
    protected Instant timestamp;
}
