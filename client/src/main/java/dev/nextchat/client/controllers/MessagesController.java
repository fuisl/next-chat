package dev.nextchat.client.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.ResourceBundle;

public class MessagesController implements Initializable {

    public Button search_msg;
    public Button menu;
    public Button more_btn;
    public TextField msg_inp;
    public Button send_btn;
    public VBox chatContainer;

    @FXML
    private void handleSendMessage() {
        String text = msg_inp.getText().trim();
        if (!text.isEmpty()) {
            // Create a new bubble
            HBox messageBubble = new HBox();
            Label messageLabel = new Label(text);
            messageLabel.getStyleClass().add("message-label");

            messageBubble.getChildren().add(messageLabel);
            messageBubble.getStyleClass().add("message-bubble");

            // Add the bubble to the chat container
            chatContainer.getChildren().add(messageBubble);

            // Clear the input
            msg_inp.clear();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
