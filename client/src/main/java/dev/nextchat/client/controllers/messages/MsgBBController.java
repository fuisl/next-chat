package dev.nextchat.client.controllers.messages;

import dev.nextchat.client.models.Message;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class MsgBBController {
    public Label msgLabel;
    public HBox bubbleBox;
    public Label senderNameLabel;

    public void setMessage(Message message, boolean isSender) {
        msgLabel.setText(message.getMessage());
        if (message.getSenderUsername() != null && !message.getSenderUsername().isEmpty()) {
            if (!isSender) {
                senderNameLabel.setText(message.getSenderUsername());
                senderNameLabel.setVisible(true);
                senderNameLabel.setManaged(true);
            }
        } else {
            senderNameLabel.setText("");
            senderNameLabel.setVisible(false);
            senderNameLabel.setManaged(false);
        }

        if (isSender) {
            bubbleBox.setAlignment(Pos.CENTER_RIGHT);
            msgLabel.setStyle("-fx-background-color: #3a54fb; -fx-text-fill: white; " +
                    "-fx-background-radius: 15 15 15 0; -fx-padding: 7 10 7 10; -fx-font-size: 15px");
        } else {
            bubbleBox.setAlignment(Pos.CENTER_LEFT);
            msgLabel.setStyle("-fx-background-color: lightgray; -fx-text-fill: black; " +
                    "-fx-background-radius: 15 15 0 15; -fx-padding: 7 10 7 10; -fx-font-size: 15px");
            senderNameLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #555555; -fx-padding: 0 0 2px 2px;");
        }
    }

}
