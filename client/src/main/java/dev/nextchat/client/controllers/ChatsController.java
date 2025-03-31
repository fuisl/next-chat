package dev.nextchat.client.controllers;

import dev.nextchat.client.models.ChatCell;
import dev.nextchat.client.models.Model;
import dev.nextchat.client.views.ChatCellFactory;
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
    public MenuItem newGroup;
    public MenuItem newMess;
    public ListView<ChatCell> listChat;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addListeners();
        initChatCells();
    }
    private void addListeners() {
        newMess.setOnAction(event -> {
            createNewMsgBox();
        });
        newGroup.setOnAction(event -> {
            createNewGroup();
        });

    }
    private void createNewGroup(){
        Model.getInstance().getViewFactory().getClientSelection().set("Group");
    }

    private void createNewMsgBox() {
        Model.getInstance().getViewFactory().getClientSelection().set("Message");
    }

    private void onChatMess() {
        Model.getInstance().getViewFactory().getClientSelectedChat().set("Messages");
    }

    private void initChatCells() {
        listChat.setItems(Model.getInstance().getChatCells());

        // Set the custom cell factory
        listChat.setCellFactory(listView -> new ChatCellFactory());
    }
}
