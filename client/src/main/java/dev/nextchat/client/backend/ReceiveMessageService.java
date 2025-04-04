package dev.nextchat.client.backend;

import javax.annotation.PostConstruct;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ReceiveMessageService implements Runnable {
    private BufferedReader reader;
    private LinkedBlockingQueue<Message> receivedMessageQueue;

    public ReceiveMessageService(BufferedReader reader, LinkedBlockingQueue<Message> receivedMessageQueue) {
        this.reader = reader;
        this.receivedMessageQueue = receivedMessageQueue;
    }

    public void run() {
        try {
            String jsonMessage;
            
            while ((jsonMessage = this.reader.readLine()) != null) {
                Message message = objectMapper.readValue(jsonMessage, Message.class);

                System.out.println("Received message for groupID: " + message.getGroupId().toString());

                this.receivedMessageQueue.put(message);
            }
        } catch (InterruptedException e){
            System.out.println("ReceiverService interrupted, shutting down thread...");
            e.printStackTrace();
            Thread.currentThread().interrupt();
            break;
        } catch (IOException e) {
            System.out.println("IO Error in ReceiverService...");
            e.printStackTrace();
            Thread.currentThread().interrupt();
            break;
        }
    }
}