package dev.nextchat.client.controllers.chats;

import dev.nextchat.client.backend.MessageController;
import dev.nextchat.client.backend.ServerResponseHandler;
import dev.nextchat.client.backend.utils.RequestFactory;
import dev.nextchat.client.models.Model;
import dev.nextchat.client.models.Model.UserDisplay;
import dev.nextchat.client.views.MemberCellFactory;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

import org.json.JSONObject;

public class NewGroupController implements Initializable, ServerResponseHandler {

    public TextField fusername;
    public Button search_btn;
    public ListView<UserDisplay> membersListView;
    public Button createGroup_btn;
    public Button return_btn;
    public Label error_lbl;

    private ObservableList<UserDisplay> selectedMembers;
    private String pendingUsernameSearch;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        selectedMembers = FXCollections.observableArrayList();

        if (membersListView != null) {
            membersListView.setItems(selectedMembers);
            membersListView.setCellFactory(listView -> new MemberCellFactory(selectedMembers));
        } else {
            System.err.println("NewGroupController: membersListView is null. Check FXML injection.");
        }

        if (createGroup_btn != null) {
            createGroup_btn.setDisable(true);
            selectedMembers.addListener((ListChangeListener<UserDisplay>) c -> updateCreateGroupButtonState());
        } else {
            System.err.println("NewGroupController: createGroupButton is null. Check FXML injection.");
        }

        addListeners();

        if (Model.getInstance().getResponseRouter() != null) {
            Model.getInstance().getResponseRouter().setNewGroupController(this);
        }
    }

    private void updateCreateGroupButtonState() {
        if (createGroup_btn != null) {
            createGroup_btn.setDisable(selectedMembers.isEmpty());
        }
    }

    private void addListeners() {
        if (return_btn != null) {
            return_btn.setOnAction(e -> {
                Model.getInstance().getViewFactory().getClientSelection().set("Chats");
                clearForm();
            });
        }

        if (search_btn != null) {
            search_btn.setOnAction(e -> {
                handleSearchUser();
                System.out.println("clicked search btn");
            });
        }
        if (fusername != null) {
            fusername.setOnAction(e -> handleSearchUser());
        }

        if (createGroup_btn != null) {
            createGroup_btn.setOnAction(e -> handleCreateGroup());
        }
    }

    private void handleSearchUser() {
        String usernameToSearch = fusername.getText().trim();
        if (usernameToSearch.isEmpty()) {
            error_lbl.setText("Please enter a username.");
            return;
        }
        if (Model.getInstance().getLoggedInUser() != null &&
                usernameToSearch.equalsIgnoreCase(Model.getInstance().getLoggedInUser())) {
            error_lbl.setText("You are already part of the group.");
            return;
        }
        if (selectedMembers.stream().anyMatch(ud -> ud.username().equalsIgnoreCase(usernameToSearch))) {
            error_lbl.setText("User already added to list.");
            return;
        }
        this.pendingUsernameSearch = usernameToSearch;
        error_lbl.setText("Searching for '" + usernameToSearch + "'...");
        JSONObject request = RequestFactory.checkIfUserExist(usernameToSearch);
        MessageController msgCtrl = Model.getInstance().getMsgCtrl();
        if (msgCtrl != null) {
            msgCtrl.getSendMessageQueue().offer(request);
        } else {
            error_lbl.setText("Error: Cannot connect to server.");
            this.pendingUsernameSearch = null;
        }
    }

    @Override
    public void onServerResponse(JSONObject response) {
        String type = response.optString("type");
        if (!"checkUserExistenceResponse".equals(type) || this.pendingUsernameSearch == null) {
            return;
        }
        String usernameInResponse = response.optString("username");
        if (!this.pendingUsernameSearch.equalsIgnoreCase(usernameInResponse)) {
            return;
        }
        final String searchedUsernameContext = this.pendingUsernameSearch;
        this.pendingUsernameSearch = null;
        boolean exists = response.optBoolean("exists", false);
        Platform.runLater(() -> {
            if (error_lbl == null) return;
            if (exists) {
                UUID userId = UUID.fromString(response.getString("userId"));
                String canonicalUsername = response.getString("username");
                if (selectedMembers.stream().anyMatch(ud -> ud.userId().equals(userId))) {
                    error_lbl.setText("User '" + canonicalUsername + "' is already added.");
                } else {
                    selectedMembers.add(new UserDisplay(userId, canonicalUsername));
                    error_lbl.setText("Added: " + canonicalUsername);
                    if (fusername != null) fusername.clear();
                }
            } else {
                error_lbl.setText("User '" + searchedUsernameContext + "' not found.");
            }
        });
    }

    private void handleCreateGroup() {
        if (selectedMembers.isEmpty()) {
            error_lbl.setText("Please add at least one member to the group.");
            return;
        }
        List<Model.UserDisplay> membersToPassToModel = new ArrayList<>(selectedMembers);
        Model.getInstance().prepareGroupCreationWithMembers(null, membersToPassToModel); // Name is auto-generated by Model

        JSONObject createReq = RequestFactory.createNewGroupRequest(
                Model.getInstance().getLoggedInUserId(),
                "New Group (ID-based name)", // Placeholder for request, Model determines actual display name
                "User created group"
        );
        Model.getInstance().getMsgCtrl().getSendMessageQueue().offer(createReq);
        clearForm();
        Model.getInstance().getViewFactory().getClientSelection().set("Chats");
    }

    private void clearForm() {
        if (fusername != null) fusername.clear();
        selectedMembers.clear();
        if (error_lbl != null) error_lbl.setText("");
        pendingUsernameSearch = null;
    }
}
