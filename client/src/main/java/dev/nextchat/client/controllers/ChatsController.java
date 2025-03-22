package dev.nextchat.client.controllers;

import dev.nextchat.client.models.Model;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import java.net.URL;
import java.util.ResourceBundle;

public class ChatsController implements Initializable {
    public Button dots_btn;
    public Button all_btn;
    public Button groups_btn;
    public Button fav_btn;

    public Text user_name;
    public ContextMenu contextMenu;
    public MenuItem starredMessages;
    public MenuItem newGroup;
    public MenuItem newMess;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addListeners();
    }
    private void addListeners() {
    }
    private void onChatMess() {
        Model.getInstance().getViewFactory().getClientSelectedChat().set("Messages");
    }
}
