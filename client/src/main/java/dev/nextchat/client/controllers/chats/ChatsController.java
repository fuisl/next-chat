package dev.nextchat.client.controllers.chats;

import dev.nextchat.client.controllers.ResponseRouter;
import dev.nextchat.client.models.ChatCell;
import dev.nextchat.client.models.Model;
import dev.nextchat.client.views.ChatCellFactory;
import dev.nextchat.client.views.SearchResultListCell;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import dev.nextchat.client.models.Model.SearchResultItem;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import java.net.URL;
import java.time.Instant;
import java.util.Comparator;
import java.util.ResourceBundle;

public class ChatsController implements Initializable {
    @FXML public Button dots_btn;
    @FXML public Button all_btn;
    @FXML public Button groups_btn;
    @FXML public Button fav_btn;
    @FXML public Text user_name;
    @FXML public ContextMenu contextMenu;
    @FXML public MenuItem newGroup;
    @FXML public MenuItem newMess;
    @FXML public ListView<ChatCell> listChat;
    @FXML public Button search;
    @FXML public TextField searchTextField;
    @FXML private ListView<SearchResultItem> searchResultsListView;

    private ResponseRouter router = Model.getInstance().getResponseRouter();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addListeners();
        initChatCells();
        user_name.textProperty().bind(Bindings.concat("Hi, ", Model.getInstance().loggedInUserProperty()));

        searchResultsListView.setItems(Model.getInstance().getSearchResults());
        searchResultsListView.setCellFactory(listView -> new SearchResultListCell());
        searchResultsListView.setOnMouseClicked(this::handleSearchResultClick);

        Model.getInstance().getSearchResults().addListener((javafx.collections.ListChangeListener.Change<? extends Model.SearchResultItem> c) -> {            boolean hasResults = !Model.getInstance().getSearchResults().isEmpty();
            searchResultsListView.setVisible(hasResults);
            searchResultsListView.setManaged(hasResults);
            listChat.setVisible(!hasResults);
            listChat.setManaged(!hasResults);
        });

        searchTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                Model.getInstance().getSearchResults().clear();
            }
        });
    }

    private void addListeners() {
        newMess.setOnAction(event -> createNewMsgBox());
        newGroup.setOnAction(event -> createNewGroup());
        search.setOnAction(event -> Model.getInstance().performSearch(searchTextField.getText()));
    }

    private void createNewGroup() {
        Model.getInstance().getViewFactory().getClientSelection().set("Group");
    }

    private void createNewMsgBox() {
        Model.getInstance().getViewFactory().getClientSelection().set("Message");
        router.setNewMessagesController(Model.getInstance().getViewFactory().getNewMsgController());
    }

    private void handleSearchResultClick(MouseEvent event) {
        SearchResultItem selected = searchResultsListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Model.getInstance().handleSearchResultSelected(selected);
        }
    }

    private void initChatCells() {
        ObservableList<ChatCell> sourceChatCells = Model.getInstance().getChatCells();
        SortedList<ChatCell> sortedChatCells = new SortedList<>(sourceChatCells);
        sortedChatCells.setComparator((c1, c2) -> {
            Instant t1 = c1.timestampProperty().get();
            Instant t2 = c2.timestampProperty().get();
            if (t1 == null && t2 == null) return 0;
            if (t1 == null) return 1;
            if (t2 == null) return -1;
            return t2.compareTo(t1);
        });
        listChat.setItems(sortedChatCells);
        listChat.setCellFactory(listView -> new ChatCellFactory());
    }
}
