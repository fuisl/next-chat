package dev.nextchat.client.views; // Same package as ChatCellFactory

import dev.nextchat.client.controllers.chats.MembersGroupController;
import dev.nextchat.client.models.Model.UserDisplay; // Assuming UserDisplay is public in Model
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox; // Specific root type of AddedMemberCell.fxml

import java.io.IOException;

public class MemberCellFactory extends ListCell<UserDisplay> {
    private final ObservableList<UserDisplay> listToRemoveFrom;
    public MemberCellFactory(ObservableList<UserDisplay> listToRemoveFrom) {
        this.listToRemoveFrom = listToRemoveFrom;
    }

    @Override
    protected void updateItem(UserDisplay userDisplay, boolean empty) {
        super.updateItem(userDisplay, empty);

        if (empty || userDisplay == null) {
            setText(null);
            setGraphic(null);
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/MembersForGroup.fxml"));
                AnchorPane cellRootNode = loader.load();
                MembersGroupController cellController = loader.getController();
                cellController.setData(userDisplay, userToRemove -> {
                    if (listToRemoveFrom != null) {
                        listToRemoveFrom.remove(userToRemove);
                    }
                });

                setGraphic(cellRootNode);
                setText(null);
            } catch (IOException e) {
                e.printStackTrace();
                setText("Error loading cell"); // Fallback
                setGraphic(null);
            }
        }
    }
}
