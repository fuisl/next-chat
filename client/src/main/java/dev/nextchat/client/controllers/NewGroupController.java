package dev.nextchat.client.controllers;

import dev.nextchat.client.models.Model;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class NewGroupController implements Initializable {
    public TextField fusername;
    public Button return_btn;

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
