package dev.nextchat.client.controllers;

import dev.nextchat.client.models.ChatCell;
import dev.nextchat.client.models.Model;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class NewMsgBoxController implements Initializable {
    public Button return_btn;
    public TextField fusername;
    public Button new_grp_btn;
    public Button chat_btn;
    private ObservableList<ChatCell> chatCells;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addListeners();
    }
    private void addListeners() {
        return_btn.setOnAction(e -> {
            Model.getInstance().getViewFactory().getClientSelection().set("Chats");
        });
        chat_btn.setOnAction(e -> {
            String enteredUsername = fusername.getText().trim();
            if (!enteredUsername.isEmpty()) {
                ChatCell newCell = new ChatCell(enteredUsername, "", null); // No message or date yet
                chatCells.add(newCell); // Add to the observable list
                System.out.println("New chat created for: " + enteredUsername);

                // Optional: go back to chat view
                Model.getInstance().getViewFactory().getClientSelection().set("Chats");
            } else {
                System.out.println("Username field is empty!");
            }
        });
    }
    public void setChatCells(ObservableList<ChatCell> chatCells) {
        this.chatCells = chatCells;
    }

}
