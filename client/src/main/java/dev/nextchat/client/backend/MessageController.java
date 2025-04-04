package dev.nextchat.client.backend;

import java.util.concurrent.LinkedBlockingQueue;

import dev.nextchat.client.models.Message;

public class MessageController {
    private final LinkedBlockingQueue<Message> receivedMessageQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Message> sendMessageQueue = new LinkedBlockingQueue<>();

    public LinkedBlockingQueue<Message> getReceivedMessageQueue() {
        return this.receivedMessageQueue;
    }

    public LinkedBlockingQueue<Message> getSendMessageQueue() {
        return this.sendMessageQueue;
    }
}
