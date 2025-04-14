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
                Message msg = receivedQueue.take();
                UUID incomingGroupId = msg.getGroupId();
                UUID myUserId = Model.getInstance().getLoggedInUserId();
                String myUsername = Model.getInstance().getLoggedInUser();

                // Resolve the sender
                String senderName = Model.getInstance()
                        .getUserIdToUsernameMap()
                        .get(msg.getSenderId());

                if (senderName == null || senderName.equals(myUsername)) {
                    // Don't process messages from myself or unknown
                    continue;
                }

                // Check if this groupId is known
                boolean isKnownGroup = Model.getInstance().getUserToGroupMap().containsValue(incomingGroupId);

                if (!isKnownGroup) {
                    // First message ever between me and sender
                    System.out.println("📩 New conversation! Creating groupID for " + senderName);

                    // Store this groupId locally for future match
                    Model.getInstance().getUserToGroupMap().put(senderName, incomingGroupId);
                }

                Platform.runLater(() -> {
                    ChatCell cell = Model.getInstance().findOrCreateChatCell(senderName);
                    cell.addMessage(msg);
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
