package dev.nextchat.client.controllers;

import dev.nextchat.client.models.Message;
import dev.nextchat.client.models.Model;
import dev.nextchat.client.models.ChatCell;
import javafx.application.Platform;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

public class MsgReceiver implements Runnable {

    private final BlockingQueue<Message> receivedQueue = Model.getInstance().getReceivedQueue();

    // Simulate incoming message from sender (User B) to the currently logged in user (User A)
    public void simulateIncomingMessage(UUID senderId) {
        String receiverUsername = Model.getInstance().getLoggedInUser(); // User A
        String senderUsername = Model.getInstance().getUserIdToUsernameMap().get(senderId); // User B

        if (senderUsername == null) {
            System.out.println("⚠ Sender username not found for: " + senderId);
            return;
        }

        // groupId assigned to the sender (userB), used to manage conversation between A & B
        UUID groupId = Model.getInstance().createGroupId(senderUsername);

        Message msg = new Message(
                UUID.randomUUID(),
                senderId,
                groupId,
                "Hey from " + senderUsername,
                Instant.now()
        );

        receivedQueue.offer(msg);
    }

    @Override
    public void run() {
        while (true) {
            try {
                Message msg = receivedQueue.take();  // Wait for message
                Platform.runLater(() -> {
                    String senderName = resolveUsernameFromSenderId(msg.getSenderId());

                    if (senderName == null) {
                        System.out.println("⚠ Could not resolve sender ID to username");
                        return;
                    }

                    // Check if there's already a chatCell with senderName, else create one
                    ChatCell cell = Model.getInstance().findOrCreateChatCell(senderName);

                    // Ensure the groupId is mapped (this is important if not already mapped)
                    Model.getInstance().createGroupId(senderName);

                    // Add message to chat history
                    cell.addMessage(msg);

                    System.out.println("📥 Message from " + senderName + " shown in User A's chat");
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    private String resolveUsernameFromSenderId(UUID senderId) {
        Map<UUID, String> map = Model.getInstance().getUserIdToUsernameMap();
        return map.get(senderId);
    }
}
