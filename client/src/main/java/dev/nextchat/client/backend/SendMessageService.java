package dev.nextchat.client.backend;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.nextchat.client.backend.model.Message;

public class SendMessageService implements Runnable {
    private PrintWriter writer;
    private LinkedBlockingQueue<Message> sendMessageQueue;
    private ObjectMapper objectMapper;
    private boolean running;

    public SendMessageService(PrintWriter writer, LinkedBlockingQueue<Message> sendMessageQueue) {
        this.writer = writer;
        this.sendMessageQueue = sendMessageQueue;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.running = true;
    }

    public void run() {
        try {
            String jsonMessage;
            
            while (!Thread.currentThread().isInterrupted()) {
                Message message = sendMessageQueue.take();

                // System.out.println("Send message for groupID: " + message.getGroupId().toString() + "with payload: " + message.getMessage());

                jsonMessage = objectMapper.writeValueAsString(message);

                writer.println(jsonMessage);
            }
        } catch (InterruptedException e){
            System.out.println("SendService interrupted, shutting down thread...");
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            System.out.println("IO Error in ReceiverService...");
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}