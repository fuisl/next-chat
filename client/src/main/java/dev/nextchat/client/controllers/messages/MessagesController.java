package dev.nextchat.client.controllers.messages;

import dev.nextchat.client.backend.MessageController;
import dev.nextchat.client.backend.utils.RequestFactory;
import dev.nextchat.client.database.MessageQueueManager;
import dev.nextchat.client.models.ChatCell;
import dev.nextchat.client.models.Model;
import dev.nextchat.client.models.Message;

import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;

import org.json.JSONObject;

import java.net.URL;
import java.time.Instant;
import java.util.ResourceBundle;
import java.util.UUID;

public class MessagesController implements Initializable {
    public Label fid;
    public Button search_msg;
    public Button menu;
    public Button more_btn;
    public TextField msg_inp;
    public Button send_btn;
    public ListView<Message> msgListView;
    public MenuItem rename;
    public MenuItem leave;

    private ChatCell currChatCell;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Model.getInstance().getViewFactory().getClientSelectedChat().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isBlank()) {
                try {
                    UUID groupId = UUID.fromString(newVal);
                    loadChatByGroupId(groupId);
                } catch (IllegalArgumentException e) {
                    if (currChatCell != null) currChatCell.getMessages().clear();
                    currChatCell = null;
                }
            } else {

                if (currChatCell != null) currChatCell.getMessages().clear();
                currChatCell = null;
                msgListView.setItems(null);
            }
        });

        String initialChatId = Model.getInstance().getViewFactory().getClientSelectedChat().get();
        if (initialChatId != null && !initialChatId.isBlank()) {
            try {
                UUID groupId = UUID.fromString(initialChatId);
                loadChatByGroupId(groupId);
            } catch (IllegalArgumentException e) {
                fid.setText("Error: Invalid Chat ID");
            }
        } else {
            fid.setText("No Chat Selected");
        }

        send_btn.setOnAction(e -> {
            String content = msg_inp.getText().trim();
            if (content.isEmpty() || currChatCell == null || currChatCell.getGroupId() == null) return;
            UUID msgId = UUID.randomUUID();
            UUID senderId = Model.getInstance().getLoggedInUserId();
            String senderName = Model.getInstance().getLoggedInUser();
            UUID groupId = currChatCell.getGroupId();
            Instant ts = Instant.now();
            Message msg = new Message(msgId, senderId, senderName, groupId, content, ts);
            MessageController backend = Model.getInstance().getMsgCtrl();
            if (backend != null) {
                JSONObject msgJson = RequestFactory.createMessageRequest(msg);
                backend.getSendMessageQueue().offer(msgJson);
            } else {
                return;
            }
            MessageQueueManager.saveMessage(msg);
            currChatCell.addMessage(msg);
            msgListView.scrollTo(currChatCell.getMessages().size() - 1);
            msg_inp.clear();
        });

        msgListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Message msg, boolean empty) {
                super.updateItem(msg, empty);
                if (empty || msg == null || msg.getSenderId() == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    boolean isSender = msg.getSenderId().equals(Model.getInstance().getLoggedInUserId());
                    Node bubble = Model.getInstance().getViewFactory().getMessageBubble(msg, isSender);
                    setGraphic(bubble);
                }
            }
        });
    }

    private void loadChatByGroupId(UUID groupId) {
        if (groupId == null) {
            fid.setText("Error: Invalid Chat");
            if (currChatCell != null) currChatCell.getMessages().clear();
            currChatCell = null;
            msgListView.setItems(null);
            return;
        }
        currChatCell = Model.getInstance().findOrCreateChatCell(groupId);
        if (currChatCell != null) {
            fid.setText(currChatCell.getOtherUsername());
            msgListView.setItems(currChatCell.getMessages());
            if (!currChatCell.getMessages().isEmpty()) {
                Platform.runLater(() -> msgListView.scrollTo(currChatCell.getMessages().size() - 1));
            }
        } else {
            fid.setText("Chat not found");
            msgListView.setItems(null);
        }
    }
}
