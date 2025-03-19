package dev.nextchat.client.controllers;

import dev.nextchat.client.models.Model;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class ChatsController implements Initializable {
    public Button dots_btn;
    public Button all_btn;
    public Button groups_btn;
    public Button fav_btn;

    public Text user_name;
    public Label fusername;
    public Label text_msg;
    public Label sent_date;
    public Button chat_btn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addListeners();
    }
    private void addListeners() {
        chat_btn.setOnAction(event -> onChatMess());
    }
    private void onChatMess() {
        Model.getInstance().getViewFactory().getClientSelectedChat().set("Messages");
    }
}
