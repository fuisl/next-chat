package dev.nextchat.client.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.nextchat.client.backend.model.Message;

public class ReceiveMessageService implements Runnable {
    private BufferedReader reader;
    private LinkedBlockingQueue<Message> receivedMessageQueue;
    private ObjectMapper objectMapper;
    private boolean running;

    public ReceiveMessageService(BufferedReader reader, LinkedBlockingQueue<Message> receivedMessageQueue) {
        this.reader = reader;
        this.receivedMessageQueue = receivedMessageQueue;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.running = true;
    }

    public void run() {
        try {
            String jsonMessage;
            
            while (running) {
                if (reader.ready()) {
                    jsonMessage = reader.readLine();

                    Message message = this.objectMapper.readValue(jsonMessage, Message.class);

                    // System.out.println("Received message for groupID: " + message.getGroupId().toString());
                    System.out.println(message.getMessage());

                    this.receivedMessageQueue.put(message);
                }                
            }
        } catch (InterruptedException e){
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