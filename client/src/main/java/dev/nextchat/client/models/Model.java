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

    public ChatCell findOrCreateChatCell(String receiver) {
        for (ChatCell cell : chatCells) {
            if (cell.senderProperty().get().equals(receiver)) {
                return cell;
            }
        }

        ChatCell newCell = new ChatCell(receiver, "", null);
        chatCells.add(newCell);
        System.out.println("New ChatCell created for: " + receiver);
        return newCell;
    }

    public ViewFactory getViewFactory() {
        return viewFactory;
    }

    public ObservableList<ChatCell> getChatCells() {
        return chatCells;
    }

    public void newChatCell(String fusername) {
        if (fusername != null && !fusername.trim().isEmpty()) {
            ChatCell newCell = new ChatCell(fusername, "", null);
            chatCells.add(newCell);
            System.out.println("New ChatCell added for user: " + fusername);
        }
    }
}
