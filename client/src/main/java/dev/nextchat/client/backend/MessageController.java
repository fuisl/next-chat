package dev.nextchat.client.backend;

import java.util.concurrent.LinkedBlockingQueue;

import dev.nextchat.client.backend.model.Message;

public class MessageController {
    private final LinkedBlockingQueue<Message> receivedMessageQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Message> sendMessageQueue = new LinkedBlockingQueue<>();
    private Thread receiveMessageThread;

    private ConnectionManager connectionManager;

    public MessageController(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public void start() {
        this.receiveMessageThread = new Thread(new ReceiveMessageService(this.connectionManager.getReader(), this.receivedMessageQueue));
        this.receiveMessageThread.start();

        try{this.receiveMessageThread.join();System.out.println("Closing thread receive.");} catch (InterruptedException e) {e.printStackTrace();}
    }

    public void stop() {
        this.receiveMessageThread.interrupt();
    }

    public LinkedBlockingQueue<Message> getReceivedMessageQueue() {
        return this.receivedMessageQueue;
    }

    public LinkedBlockingQueue<Message> getSendMessageQueue() {
        return this.sendMessageQueue;
    }
}
