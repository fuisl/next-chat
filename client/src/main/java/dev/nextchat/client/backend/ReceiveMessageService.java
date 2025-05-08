package dev.nextchat.client.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONObject;

import dev.nextchat.client.backend.model.Message;

public class ReceiveMessageService implements Runnable {
    private BufferedReader reader;
    private LinkedBlockingQueue<JSONObject> receivedMessageQueue;
    private boolean running;

    public ReceiveMessageService(BufferedReader reader, LinkedBlockingQueue<JSONObject> receivedMessageQueue) {
        this.reader = reader;
        this.receivedMessageQueue = receivedMessageQueue;
        this.running = true;
    }

    public void run() {
        try {
            String rawMessage;
            JSONObject json;

            while (running) {
                if (reader.ready()) {
                    rawMessage = reader.readLine();
                    json = new JSONObject(rawMessage);

                    this.receivedMessageQueue.put(json);
                }
            }
        } catch (InterruptedException e) {
            this.running = false;
            System.out.println("ReceiveService interrupted, shutting down thread...");
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            System.out.println("IO Error in ReceiverService...");
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}
