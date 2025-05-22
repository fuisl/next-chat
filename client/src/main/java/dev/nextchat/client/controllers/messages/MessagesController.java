package dev.nextchat.client.controllers.messages;

import dev.nextchat.client.backend.MessageController;
import dev.nextchat.client.backend.utils.MessageParser;
import dev.nextchat.client.backend.utils.RequestFactory;
import dev.nextchat.client.controllers.ResponseRouter;
import dev.nextchat.client.database.MessageQueueManager;
import dev.nextchat.client.models.ChatCell;
import dev.nextchat.client.models.Model;
import dev.nextchat.client.models.Message;

import javafx.application.Platform;
import javafx.fxml.FXML;
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
    public Label fid;
    public Button search_msg;
    public Button menu;
    public Button more_btn;
    public TextField msg_inp;
    public Button send_btn;
    public ListView<Message> msgListView;
    private ChatCell currentChatCell;
    private final Queue<String> pendingMessages = new LinkedList<>();
    private final MessageController msgCtrl = Model.getInstance().getMsgCtrl();
    private ResponseRouter router = Model.getInstance().getResponseRouter();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Listen for chat selection changes
        Model.getInstance()
                .getViewFactory()
                .getClientSelectedChat()
                .addListener((obs, oldVal, newVal) -> loadChat(newVal));
        // Load initial chat
        loadChat(Model.getInstance().getViewFactory().getClientSelectedChat().get());

        // Send button action
        send_btn.setOnAction(e -> {
            String content = msg_inp.getText().trim();
            if (content.isEmpty()) return;


        });

        // Custom cell factory for message bubbles
        msgListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Message msg, boolean empty) {
                super.updateItem(msg, empty);
                if (empty || msg == null) {
                    setGraphic(null);
                } else {
                    boolean isSender = msg.getSenderId().equals(
                            Model.getInstance().getLoggedInUserId());
                    Node bubble = Model.getInstance()
                            .getViewFactory()
                            .getMessageBubble(msg, isSender);
                    setGraphic(bubble);
                }
            }
        });
    }

    private void loadChat(String username) {
        if (username != null && !username.isBlank()) {
            System.out.println("[MessagesController] Loading chat with: " + username);
            fid.setText(username);
            // Retrieve or initiate chat (may trigger createGroupRequest)
            currentChatCell = Model.getInstance().findOrCreateChatCell(username);
            msgListView.setItems(currentChatCell.getMessages());
            pendingMessages.clear();
        }
    }

    public void handleCreateGroupResponse(JSONObject json) {
        UUID groupId       = UUID.fromString(json.getString("groupId"));
        String otherUsername = Model.getInstance()
                .getViewFactory()
                .getClientSelectedChat()
                .get();
            // <-- no fallback
        UUID me            = Model.getInstance().getLoggedInUserId();
        UUID otherId       = Model.getInstance().getUserId(otherUsername);

        Model.getInstance().persistGroupMapping(me, otherId, groupId);

        JSONObject joinReq = RequestFactory.createJoinGroupRequest(otherId, groupId);
        msgCtrl.getSendMessageQueue().offer(joinReq);
        System.out.println("[MessagesController] Sent join_group request: " + joinReq);
    }

}
