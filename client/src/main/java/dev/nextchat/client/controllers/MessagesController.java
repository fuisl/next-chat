package dev.nextchat.client.controllers;

import dev.nextchat.client.models.ChatCell;
import dev.nextchat.client.models.Model;
import dev.nextchat.client.models.Message;
import java.time.LocalDateTime;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

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
    public ScrollPane sp_main;
    private String fusername;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chatContainer.heightProperty().addListener((observable, oldValue, newValue) -> {
            sp_main.setVvalue((Double) newValue);
        });

        send_btn.setOnAction(e -> {
            String sender = "me"; // Replace with: Model.getInstance().getCurrentUser().getUsername()
            String receiver = Fid.getText();
            String msg = msg_inp.getText().trim();
            Message message = new Message(sender, receiver, msg, LocalDateTime.now()); // receiver == groupID

            ChatCell cell = Model.getInstance().findOrCreateChatCell(receiver);
            cell.txtMsgProperty().set(msg);
            cell.timestampProperty().set(message.getTimestamp());

            if (!msg.isEmpty()){
                HBox hbox = new HBox();
                hbox.setAlignment(Pos.CENTER_RIGHT);
                hbox.setPadding(new Insets(5,5,-3,10));
                Text text = new Text(msg);
                TextFlow textFlow = new TextFlow(text);

                textFlow.setStyle("-fx-color: white;" +
                        " -fx-background-color: #3a54fb;" +
                        "-fx-background-radius: 15px;" +
                        "-fx-font-size: 14px;");

                textFlow.setPadding(new Insets(5,10,5,10));
                text.setFill(Color.color(0.934,0.945,0.996));

                hbox.getChildren().add(textFlow);
                chatContainer.getChildren().add(hbox);

            }


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
