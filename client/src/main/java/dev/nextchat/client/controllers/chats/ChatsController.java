package dev.nextchat.client.controllers.chats;

import dev.nextchat.client.controllers.ResponseRouter;
import dev.nextchat.client.models.ChatCell;
import dev.nextchat.client.models.Model;
import dev.nextchat.client.views.ChatCellFactory;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import java.net.URL;
import java.time.Instant;
import java.util.Comparator;
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
    private ResponseRouter router = Model.getInstance().getResponseRouter();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addListeners();
        initChatCells();
        user_name.textProperty().bind(Bindings.concat("Hi, ", Model.getInstance().loggedInUserProperty()));
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
        router.setNewMessagesController(Model.getInstance().getViewFactory().getNewMsgController());
    }

    private void onChatMess() {
        Model.getInstance().getViewFactory().getClientSelectedChat().set("Messages");
    }

    private void initChatCells() {
        ObservableList<ChatCell> sourceChatCells = Model.getInstance().getChatCells();
        SortedList<ChatCell> sortedChatCells = new SortedList<>(sourceChatCells);

        sortedChatCells.setComparator(new Comparator<ChatCell>() {
            @Override
            public int compare(ChatCell c1, ChatCell c2) {
                Instant t1 = c1.timestampProperty().get();
                Instant t2 = c2.timestampProperty().get();

                if (t1 == null && t2 == null) {
                    return 0;
                }
                if (t1 == null) {
                    return 1;
                }
                if (t2 == null) {
                    return -1;
                }
                return t2.compareTo(t1);
            }
        });

        listChat.setItems(sortedChatCells);
        listChat.setCellFactory(listView -> new ChatCellFactory());
    }
}
