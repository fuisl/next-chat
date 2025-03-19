package dev.nextchat.client.controllers;

import dev.nextchat.client.models.Model;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ResourceBundle;

public class ClientController implements Initializable {

    public BorderPane client_parent;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Model.getInstance().getViewFactory().getClientSelectedChat().addListener((observable, oldVal, newVal) -> {
            if (newVal != null) {
                client_parent.setRight(Model.getInstance().getViewFactory().getMsgView());
            }
        });
    }


}
