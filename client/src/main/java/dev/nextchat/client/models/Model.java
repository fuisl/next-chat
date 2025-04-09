package dev.nextchat.client.models;

import dev.nextchat.client.database.UserDatabase;
import dev.nextchat.client.views.ViewFactory;
import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.UUID;

public class Model {
    private  static Model model;
    private final ViewFactory viewFactory;
    private final ObservableList<ChatCell> chatCells;
    private final StringProperty loggedInUser = new SimpleStringProperty();

    private Model() {
        this.viewFactory = new ViewFactory();
        this.chatCells = FXCollections.observableArrayList();
    }

    public static synchronized Model getInstance() {
        if (model == null) {
            model = new Model();
        }
        return model;
    }

    public StringProperty loggedInUserProperty() {
        return loggedInUser;
    }

    public String getLoggedInUser() {
        return loggedInUser.get();
    }

    public void setLoggedInUser(String username) {
        this.loggedInUser.set(username);
    }
    public boolean login(String username, String password) {
        boolean valid = UserDatabase.authenticate(username, password);
        if (valid) setLoggedInUser(username); // Set here!
        return valid;
    }

    public boolean registerUser(String username, String password) {
        try {
            if (UserDatabase.userExists(username)) {
                return false; // User already exists
            }

            Client newUser = new Client(username, password);
            UserDatabase.registerUser(newUser);
            setLoggedInUser(username); // optional: auto-login
            return true;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public ChatCell findOrCreateChatCell(String fusername) {
        Optional<ChatCell> existing = chatCells.stream() // Avoid nullPointer for debug
                .filter(c -> c.senderProperty().get().equals(fusername))
                .findFirst();

        if (existing.isPresent()) {
            // UUID groupId = getGroupId(fusername);
            //System.out.println("Found existing ChatCell for user '" + fusername + "' with groupId: " + groupId);
            return existing.get();
        } else {
            // UUID groupId = createGroupId(fusername);
            ChatCell cell = new ChatCell(fusername, "", null);
            chatCells.add(cell);
            //System.out.println("Created new ChatCell for user '" + fusername + "' with groupId: " + groupId);
            return cell;
        }
    }

    public ViewFactory getViewFactory() {
        return viewFactory;
    }

    public ObservableList<ChatCell> getChatCells() {
        return chatCells;
    }
}
