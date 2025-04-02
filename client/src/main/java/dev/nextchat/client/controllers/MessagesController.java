package dev.nextchat.client.controllers;

import dev.nextchat.client.models.ChatCell;
import dev.nextchat.client.models.Model;
import dev.nextchat.client.models.Message;
import java.time.LocalDateTime;
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
            String sender = "me"; // Replace with: Model.getInstance().getCurrentUser().getUsername()
            String receiver = Fid.getText();
            String msg = msg_inp.getText().trim();
            Message message = new Message(sender, receiver, msg, LocalDateTime.now()); // receiver == groupID

            ChatCell cell = Model.getInstance().findOrCreateChatCell(receiver);
            cell.txtMsgProperty().set(msg);
            cell.timestampProperty().set(message.getTimestamp());

            Label messageLabel = new Label("Me: " + msg);
            messageLabel.setWrapText(true);
            messageLabel.setStyle("-fx-background-color: lightblue; -fx-padding: 10; -fx-background-radius: 8;");
            chatContainer.getChildren().add(messageLabel);

            msg_inp.clear();
            System.out.println("Built message: " + message);

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
