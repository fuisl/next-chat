package dev.nextchat.client.controllers;

import dev.nextchat.client.models.Model;
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addListeners();
    }
    private void addListeners() {
        return_btn.setOnAction(e -> {
            Model.getInstance().getViewFactory().getClientSelection().set("Chats");
        });
    }
}
