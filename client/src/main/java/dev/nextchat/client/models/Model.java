package dev.nextchat.client.models;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import dev.nextchat.client.database.UserDatabase;
import dev.nextchat.client.views.ViewFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Model {
    private  static Model model;
    private final ViewFactory viewFactory;
    private final ObservableList<ChatCell> chatCells;
    private final StringProperty loggedInUser = new SimpleStringProperty();
    private final LinkedBlockingQueue<Message> sendMessageQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Message> receivedMessageQueue = new LinkedBlockingQueue<>();
    private final Map<String, UUID> userToGroupMap = new HashMap<>(); // Group for 1
    BiMap<String, UUID> userIdMap = HashBiMap.create();

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

    public void preloadUserIdMapFromJson() {
        try {
            for (User user : UserDatabase.loadUsers()) {
                String username = user.getUsername();
                UUID userId = user.getUserId();
    
                if (username != null && userId != null) {
                    userIdMap.put(username, userId);
                } else {
                    System.err.println("Skipped user due to null: username=" + username + ", userId=" + userId);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    


    public StringProperty loggedInUserProperty() {
        return loggedInUser;
    }

    public String getLoggedInUser() {
        return loggedInUser.get();
    }

    public UUID getLoggedInUserId() {
        return userIdMap.get(getLoggedInUser());
    }


    public void setLoggedInUser(String username) {
        this.loggedInUser.set(username);
    }

    public Map<String, UUID> getUserToGroupMap() {
        return userToGroupMap;
    }
    public Map<UUID, String> getUserIdToUsernameMap() {
        return userIdMap.inverse();
    }

    public boolean login(String username, String password) {
        boolean valid = UserDatabase.authenticate(username, password);
        if (valid) setLoggedInUser(username);
        return valid;
    }


    public boolean registerUser(String username, String password) {
        try {
            if (UserDatabase.userExists(username)) {
                return false; // User already exists
            }

            UUID uuid = UUID.randomUUID();
            User newUser = new User(uuid, username, password);
            UserDatabase.registerUser(newUser);
            setLoggedInUser(username);
            userIdMap.put(username, uuid);
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
            UUID groupId = getGroupId(fusername);
            System.out.println("Found existing ChatCell for user '" + fusername + "' with groupId: " + groupId);
            return existing.get();
        } else {
            UUID groupId = createGroupId(fusername);
            System.out.println("Created new ChatCell for user '" + fusername + "' with groupId: " + groupId);
            ChatCell cell = new ChatCell(fusername, "", null);
            chatCells.add(cell);
            return cell;
        }
    }

    public ViewFactory getViewFactory() {
        return viewFactory;
    }

    public ObservableList<ChatCell> getChatCells() {
        return chatCells;
    }

    public UUID createGroupId(String fusername) {
        return userToGroupMap.computeIfAbsent(fusername, k -> UUID.randomUUID());
    }

    public UUID getGroupId(String fusername) {
        return userToGroupMap.get(fusername);
    }

    public LinkedBlockingQueue<Message> getSendQueue() {
        return sendMessageQueue;
    }

    public LinkedBlockingQueue<Message> getReceivedQueue() {
        return receivedMessageQueue;
    }
}
