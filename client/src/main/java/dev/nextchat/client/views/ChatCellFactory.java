package dev.nextchat.client.views;

import dev.nextchat.client.controllers.chats.ChatCellController;
import dev.nextchat.client.models.ChatCell;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;

import java.io.IOException;

public class ChatCellFactory extends ListCell<ChatCell> {
    @Override
    protected void updateItem(ChatCell chatCell, boolean empty) {
        super.updateItem(chatCell, empty);
        if (empty){
            setText(null);
            setGraphic(null);
        } else {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/ChatCell.fxml"));
            ChatCellController controller = new ChatCellController(chatCell);
            loader.setController(controller);
            setText(null);
            try {
                setGraphic(loader.load());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
}
