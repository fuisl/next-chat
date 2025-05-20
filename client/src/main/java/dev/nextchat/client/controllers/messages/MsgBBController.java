package dev.nextchat.client.controllers.messages;

import dev.nextchat.client.models.Message;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class MsgBBController {
    public Label msgLabel;
    public HBox bubbleBox;

    public void setMessage(Message message, boolean isSender) {
        msgLabel.setText(message.getMessage());

        if (isSender) {
            bubbleBox.setAlignment(Pos.CENTER_RIGHT);
            msgLabel.setStyle("-fx-background-color: #3a54fb; -fx-text-fill: white; " +
                    "-fx-background-radius: 15; -fx-padding: 7,7,-3,10; -fx-font-size: 15px");
        } else {
            bubbleBox.setAlignment(Pos.CENTER_LEFT);
            msgLabel.setStyle("-fx-background-color: lightgray; -fx-text-fill: black; " +
                    "-fx-background-radius: 15; -fx-padding: 5,5,-3,10; -fx-font-size: 15px");
        }
    }
}
