package dev.nextchat.client.controllers;

import dev.nextchat.client.models.Model;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ResourceBundle;

public class ClientController implements Initializable {

    public BorderPane client_parent;
    private Node originalCenter;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        originalCenter = client_parent.getCenter();
        Model.getInstance().getViewFactory().getClientSelectedChat().addListener((observable, oldVal, newVal) -> {
            if (newVal != null) {
                client_parent.setRight(Model.getInstance().getViewFactory().getMsgView());
            }
        });

        Model.getInstance().getViewFactory().getClientSelection().addListener((observable, oldVal, newVal) -> {
            if (newVal != null) {
                client_parent.setCenter(Model.getInstance().getViewFactory().getNewMsgWindow());
            }
            if ("Chats".equals(newVal)){
                client_parent.setCenter(originalCenter);
            }
        });
    }


}
