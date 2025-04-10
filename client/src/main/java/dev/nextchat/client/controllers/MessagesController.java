package dev.nextchat.client.controllers;

import dev.nextchat.client.models.ChatCell;
import dev.nextchat.client.models.Model;
import dev.nextchat.client.models.Message;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.UUID;

public class MessagesController implements Initializable {

    public Button search_msg;
    public Button menu;
    public Button more_btn;
    public TextField msg_inp;
    public Button send_btn;
    public Label Fid;
    public ListView<Message> msgListView;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        send_btn.setOnAction(e -> {
            String content = msg_inp.getText().trim();
            if (content.isEmpty()) return;

            UUID groupId = Model.getInstance().createGroupId(Fid.getText().trim());
            UUID senderId = Model.getInstance().getLoggedInUserId();

            Message msg = new Message(UUID.randomUUID(),senderId, groupId, content, Instant.now());


            // Store locally in chat history
            ChatCell chat = Model.getInstance().findOrCreateChatCell(Fid.getText().trim());
            chat.addMessage(msg);

            msg_inp.clear();
        });

        // Loading new chat
        Model.getInstance().getViewFactory().getClientSelectedChat().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isBlank()) {
                loadChat(newVal);
            }
        });

        // Initial load on startup
        String currentUser = Model.getInstance().getViewFactory().getClientSelectedChat().get();
        if (currentUser != null && !currentUser.isBlank()) {
            loadChat(currentUser);
        }

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
    private void loadChat(String username) {
        Fid.setText(username);
        ChatCell selectedChat = Model.getInstance().findOrCreateChatCell(username);
        msgListView.setItems(selectedChat.getMessages());
    }


}
