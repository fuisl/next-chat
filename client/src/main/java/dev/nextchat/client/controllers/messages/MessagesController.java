package dev.nextchat.client.controllers.messages;

import dev.nextchat.client.backend.MessageController;
import dev.nextchat.client.backend.utils.MessageParser;
import dev.nextchat.client.backend.utils.RequestFactory;
import dev.nextchat.client.database.MessageQueueManager;
import dev.nextchat.client.models.ChatCell;
import dev.nextchat.client.models.Model;
import dev.nextchat.client.models.Message;

import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import org.json.JSONObject;
import java.net.URL;
import java.time.Instant;
import java.util.LinkedList;
import java.util.Queue;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MessagesController implements Initializable {

    public Button search_msg;
    public Button menu;
    public Button more_btn;
    public TextField msg_inp;
    public Button send_btn;
    public Label fid;
    public ListView<Message> msgListView;

    // Holds the current conversation group ID once created
    private UUID currentGroupId;

    // Pending messages before a group ID is returned
    private final Queue<String> pendingMessages = new LinkedList<>();

    private ScheduledExecutorService messagePollingExecutor;
    private final MessageController msgCtrl = Model.getInstance().getMessageController();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Start polling incoming messages
        startMessagePolling();

        // Initialize send button logic
        send_btn.setOnAction(e -> {
            String content = msg_inp.getText().trim();
            if (content.isEmpty()) return;

            UUID senderId = Model.getInstance().getLoggedInUserId();
            String groupName = fid.getText().trim();
            String description = "";

            if (currentGroupId == null) {
                // First message: request server to create/fetch a group
                pendingMessages.add(content);
                JSONObject req = RequestFactory.createNewGroupRequest(senderId, groupName,description);
                msgCtrl.getSendMessageQueue().offer(req);
                System.out.println("[MessagesController] Requested group creation: " + req);
            } else {
                // Already have group ID: send message directly
                Message msg = new Message(UUID.randomUUID(), senderId, currentGroupId, content, Instant.now());
                JSONObject message = RequestFactory.createMessageRequest(msg);
                msgCtrl.getSendMessageQueue().offer(message);
                System.out.println("[MessagesController] Queued message: " + message);

                // Locally persist and display
                MessageQueueManager.saveMessage(msg);
                ChatCell chat = Model.getInstance().findOrCreateChatCell(groupName);
                chat.addMessage(msg);
            }
            msg_inp.clear();
        });

        // Update chat view when a new contact is selected
        Model.getInstance().getViewFactory().getClientSelectedChat().addListener((obs, oldVal, newVal) -> loadChat(newVal));
        loadChat(Model.getInstance().getViewFactory().getClientSelectedChat().get());

        // Custom cell renderer
        msgListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Message msg, boolean empty) {
                super.updateItem(msg, empty);
                if (empty || msg == null) {
                    setGraphic(null);
                } else {
                    boolean isSender = msg.getSenderId().equals(Model.getInstance().getLoggedInUserId());
                    Node bubble = Model.getInstance().getViewFactory().getMessageBubble(msg, isSender);
                    setGraphic(bubble);
                }
            }
        });
    }

    private void startMessagePolling() {
        messagePollingExecutor = Executors.newSingleThreadScheduledExecutor();
        LinkedBlockingQueue<JSONObject> queue = msgCtrl.getReceivedMessageQueue();

        messagePollingExecutor.scheduleAtFixedRate(() -> {
            JSONObject json = queue.poll();
            if (json != null) {
                System.out.println("[MessagesController] Polled message: " + json);
                Platform.runLater(() -> handleIncomingMessage(json));
            }
        }, 0, 200, TimeUnit.MILLISECONDS);
    }

    private void loadChat(String username) {
        if (username != null && !username.isBlank()) {
            fid.setText(username);
            ChatCell selectedChat = Model.getInstance().findOrCreateChatCell(username);
            msgListView.setItems(selectedChat.getMessages());

            // Reset groupId when switching chats
            currentGroupId = null;
            pendingMessages.clear();
        }
    }

    /**
     * Handles server messages, including group creation responses and chat messages.
     */
    private void handleIncomingMessage(JSONObject json) {
        String type = json.optString("type");
        switch (type) {
            case "createGroupResponse" -> {
                // Server gave us the groupId
                String gid = json.getString("groupId");
                currentGroupId = UUID.fromString(gid);
                System.out.println("[MessagesController] Received group ID: " + currentGroupId);

                // Flush any messages we stashed while waiting for that ID
                while (!pendingMessages.isEmpty()) {
                    String text = pendingMessages.poll();
                    // build a Message object
                    Message msg = new Message(UUID.randomUUID(), Model.getInstance().getLoggedInUserId(), currentGroupId, text, Instant.now());
                    // now use the new createMessageRequest(Message) API
                    JSONObject msgReq = RequestFactory.createMessageRequest(msg);
                    msgCtrl.getSendMessageQueue().offer(msgReq);
                    System.out.println("[MessagesController] Flushed pending message: " + msgReq);

                    // and keep your local history in sync
                    MessageQueueManager.saveMessage(msg);
                    Model.getInstance()
                            .findOrCreateChatCell(fid.getText().trim())
                            .addMessage(msg);
                }
            }
            case "sendMessageResponse" -> {
                System.out.println("[MessagesController] sendMessageResponse: " + json);
            }
            case "message" -> {
                // An actual chat message from someone else
                Message msg = MessageParser.fromJson(json);
                MessageQueueManager.saveMessage(msg);
                Model.getInstance().renderMessage(
                        msg,
                        Model.getInstance().getLoggedInUserId()
                );
            }
            default -> {
                System.out.println("[MessagesController] Unhandled message type: " + type);
            }
        }
    }

}
