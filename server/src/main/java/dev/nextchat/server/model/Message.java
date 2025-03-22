package dev.nextchat.server.model;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;

public abstract class Message {

    @Id
    protected UUID id;
    protected UUID groupId;
    protected String message;
    protected Instant timestamp;
}
