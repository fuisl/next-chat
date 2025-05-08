package dev.nextchat.client.backend;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONObject;

import dev.nextchat.client.backend.model.Message;

public class SendMessageService implements Runnable {
    private PrintWriter writer;
    private LinkedBlockingQueue<JSONObject> sendMessageQueue;
    private boolean running;

    public SendMessageService(PrintWriter writer, LinkedBlockingQueue<JSONObject> sendMessageQueue) {
        this.writer = writer;
        this.sendMessageQueue = sendMessageQueue;
        this.running = true;
    }

    public void run() {
        try {
            String rawMessage;

            while (!Thread.currentThread().isInterrupted()) {
                JSONObject jsonMessage = sendMessageQueue.take();

                writer.println(jsonMessage.toString());
            }
        } catch (InterruptedException e) {
            System.out.println("SendService interrupted, shutting down thread...");
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}
