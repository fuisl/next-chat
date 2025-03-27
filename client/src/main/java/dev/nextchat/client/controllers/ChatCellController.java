package dev.nextchat.client.controllers;

import dev.nextchat.client.models.ChatCell;
import dev.nextchat.client.models.Model;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class ChatCellController implements Initializable {
    public Button chat_msg_btn;
    public Label fusername;
    public Label txt_msg;
    public Label txt_date;

    private final ChatCell cell;

    public ChatCellController(ChatCell cell) {
        this.cell = cell;
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fusername.textProperty().bind(cell.senderProperty());
        txt_msg.textProperty().bind(cell.txtMsgProperty());
        txt_date.textProperty().bind(cell.dateProperty().asString());
    }
    @FXML
    public void onChatButtonClick() {
        String selectedUser = cell.senderProperty().get();
        System.out.println("Selected chat with: " + selectedUser);
        Model.getInstance().getViewFactory().getClientSelectedChat().set(selectedUser);
    }
}
