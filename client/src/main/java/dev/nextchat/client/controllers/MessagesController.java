package dev.nextchat.client.controllers;

import dev.nextchat.client.backend.MessageController;
import dev.nextchat.client.backend.utils.MessageParser;
import dev.nextchat.client.backend.utils.RequestFactory;
import dev.nextchat.client.database.MessageQueueManager;
import dev.nextchat.client.models.ChatCell;
import dev.nextchat.client.models.Model;
import dev.nextchat.client.models.Message;

import java.time.Instant;

import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import org.json.JSONObject;

import java.net.URL;
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
    private ScheduledExecutorService messagePollingExecutor;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        startMessagePolling();
        send_btn.setOnAction(e -> {
            String content = msg_inp.getText().trim();
            if (content.isEmpty()) return;

            UUID senderId = Model.getInstance().getLoggedInUserId();
            String senderName = Model.getInstance().getLoggedInUser();
            UUID groupId = Model.getInstance().getOrCreateGroupId(senderName, fid.getText().trim());
            Message msg = new Message(UUID.randomUUID(),senderId,groupId,content,Instant.now());

            JSONObject message = RequestFactory.createMessageRequest(msg);
            MessageController msgCtrl = Model.getInstance().getMessageController();
            msgCtrl.getSendMessageQueue().offer(message);
            System.out.println("[MessagesController] Queued message: " + message.toString());

            // Store locally in chat history
            MessageQueueManager.saveMessage(msg);
            ChatCell chat = Model.getInstance().findOrCreateChatCell(fid.getText().trim());
            chat.addMessage(msg);

            msg_inp.clear();
        });

        // Loading new chat
        Model.getInstance().getViewFactory().getClientSelectedChat().addListener((obs, oldVal, newVal) -> {
            loadChat(newVal);
        });
        loadChat(Model.getInstance().getViewFactory().getClientSelectedChat().get());

        // Custom message bubble rendering
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

        messagePollingExecutor.scheduleAtFixedRate(() -> {
            LinkedBlockingQueue<JSONObject> queue = Model.getInstance()
                    .getMessageController()
                    .getReceivedMessageQueue();

            int size = queue.size();
            if (size > 0) {
                System.out.println("📥 Received queue size: " + size);
            }

            JSONObject json = queue.poll();

            if (json != null) {
                System.out.println("📩 Polled new message from queue: " + json);
                Platform.runLater(() -> handleIncomingMessage(json));
            }
        }, 0, 200, TimeUnit.MILLISECONDS); // poll every 200ms
    }

    private void loadChat(String username) {
        if (username != null && !username.isBlank()) {
            fid.setText(username);
            ChatCell selectedChat = Model.getInstance().findOrCreateChatCell(username);
            msgListView.setItems(selectedChat.getMessages());
        }
    }

    private void handleIncomingMessage(JSONObject json) {
        // ✅ Convert JSON to Message object
        Message msg = MessageParser.fromJson(json);

        // ✅ Save to messages.json
        MessageQueueManager.saveMessage(msg);

        // ✅ Render this message into the UI (ChatCell)
        Model.getInstance().renderMessage(msg, Model.getInstance().getLoggedInUserId());
    }



}
