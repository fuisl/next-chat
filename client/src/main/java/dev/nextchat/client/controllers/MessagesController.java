package dev.nextchat.client.controllers;

import dev.nextchat.client.models.Model;
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
    public Label Fid;
    private String fusername;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        send_btn.setOnAction(e -> {
            String message = msg_inp.getText().trim();

            if (!message.isEmpty()) {
                Label messageLabel = new Label("Me: " + message);
                messageLabel.setWrapText(true);
                messageLabel.setStyle("-fx-background-color: lightblue; -fx-padding: 10; -fx-background-radius: 8;");

                chatContainer.getChildren().add(messageLabel);
                msg_inp.clear();
            }
        });

        Model.getInstance().getViewFactory().getClientSelectedChat().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isBlank()) {
                Fid.setText(newVal);

            }
        });

        String currentUser = Model.getInstance().getViewFactory().getClientSelectedChat().get();
        if (currentUser != null && !currentUser.isBlank()) {
            Fid.setText(currentUser);
        }
    }
}
