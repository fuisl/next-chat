package dev.nextchat.client.backend;

import java.io.*;
import java.nio.Buffer;
import java.time.Instant;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONObject;

import dev.nextchat.client.backend.model.Message;

public class MessageController {
    private final LinkedBlockingQueue<JSONObject> receivedMessageQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<JSONObject> sendMessageQueue = new LinkedBlockingQueue<>();
    private Thread receiveMessageThread;
    private Thread sendMessageThread;

    private ConnectionManager connectionManager;

    public MessageController(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public void start() {
        this.receiveMessageThread = new Thread(
                new ReceiveMessageService(this.connectionManager.getReader(), this.receivedMessageQueue));
        this.receiveMessageThread.start();

        this.sendMessageThread = new Thread(
                new SendMessageService(this.connectionManager.getWriter(), this.sendMessageQueue));
        this.sendMessageThread.start();
        System.out.println("[MessageController] Send thread started");
    }

    public void stop() {
        System.out.println("Shutting down services");

        this.receiveMessageThread.interrupt();
        this.sendMessageThread.interrupt();

        try {
            this.receiveMessageThread.join();
            System.out.println("Closing thread receive.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            this.sendMessageThread.join();
            System.out.println("Closing thread send.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public LinkedBlockingQueue<JSONObject> getReceivedMessageQueue() {
        return this.receivedMessageQueue;
    }

    public LinkedBlockingQueue<JSONObject> getSendMessageQueue() {
        return this.sendMessageQueue;
    }
}
