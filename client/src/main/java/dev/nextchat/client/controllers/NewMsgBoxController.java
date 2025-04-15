package dev.nextchat.client.controllers;

import dev.nextchat.client.models.ChatCell;
import dev.nextchat.client.models.Model;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class NewMsgBoxController implements Initializable {
    public Button return_btn;
    public TextField fusername;
    public Button new_grp_btn;
    public Button chat_btn;
    public Label Uid;
    public Button self_chat;
    public Label error_lbl;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addListeners();
        Uid.setText(Model.getInstance().getLoggedInUser() + " (You)");

    }
    private void addListeners() {
        return_btn.setOnAction(e -> {
            Model.getInstance().getViewFactory().getClientSelection().set("Chats");
        });
        self_chat.setOnAction(e -> {

            String enteredUsername = Uid.getText().trim(); // Uid = AndrwPham(demo)
            if (!enteredUsername.isEmpty()) {
                Model.getInstance().findOrCreateChatCell(enteredUsername);
                Model.getInstance().getViewFactory().getClientSelectedChat().set(enteredUsername);
                Model.getInstance().getViewFactory().getClientSelection().set("Chats");
            }
        });

        chat_btn.setOnAction(e -> {
            String enteredUsername = fusername.getText().trim(); // from input field

            if (enteredUsername.isEmpty()) {
                error_lbl.setText("Please enter a username.");
                return;
            }

            if (!Model.getInstance().userExists(enteredUsername)) {
                error_lbl.setText("User does not exist.");
                return;
            }

            Model.getInstance().findOrCreateChatCell(enteredUsername);
            Model.getInstance().getViewFactory().getClientSelectedChat().set(enteredUsername);
            Model.getInstance().getViewFactory().getClientSelection().set("Chats");
        });
        new_grp_btn.setOnAction(e -> {
            Model.getInstance().getViewFactory().getClientSelection().set("Group");
        });
    }



}
