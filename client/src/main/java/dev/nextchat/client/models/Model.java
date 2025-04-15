package dev.nextchat.client.models;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import dev.nextchat.client.database.UserDatabase;
import dev.nextchat.client.views.ViewFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import dev.nextchat.client.database.MessageQueueManager;

public class Model {
    private  static Model model;
    private final ViewFactory viewFactory;
    private final ObservableList<ChatCell> chatCells;
    private final StringProperty loggedInUser = new SimpleStringProperty();
    private final LinkedBlockingQueue<Message> sendMessageQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Message> receivedMessageQueue = new LinkedBlockingQueue<>();
    private final Map<String, UUID> userToGroupMap = new HashMap<>(); // Group for 1
    BiMap<String, UUID> userIdMap = HashBiMap.create();
    private final MessageQueueManager messageQueueManager = new MessageQueueManager();


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

    public boolean isUserInGroup(UUID userId, UUID groupId) {
        return userToGroupMap.containsValue(groupId); // simplistic check
    }
    public Map<UUID, String> getUserIdToUsernameMap() {
        return userIdMap.inverse();
    }

    public boolean userExists(String username) {
        return userIdMap.containsKey(username);
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

    public void loadMessagesForUser(UUID loggedInUserId) {
        List<Message> allMessages = messageQueueManager.loadMessages();

        for (Message msg : allMessages) {
            UUID groupId = msg.getGroupId();
            UUID senderId = msg.getSenderId();

            if (isUserInGroup(loggedInUserId, groupId)) {
                String senderUsername = userIdMap.inverse().get(senderId);

                if (!senderId.equals(loggedInUserId)) {
                    // Only create ChatCell for others
                    ChatCell cell = findOrCreateChatCell(senderUsername);
                    cell.addMessage(msg);

                }
            }
        }
    }



    public ChatCell findOrCreateChatCell(String fusername) {

        String selfUsername = getLoggedInUser();
        System.out.println("This is " + selfUsername);
        if (fusername.equals(selfUsername)) {
            System.out.println("Skipped creating ChatCell for self: " + fusername);
            return null;
        }
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
            logUsersInGroup(groupId);
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


    public MessageQueueManager getMessageQueueManager() {
        return messageQueueManager;
    }
    public void logUsersInGroup(UUID groupId) {
        System.out.println("Users in groupId: " + groupId);

        for (Map.Entry<String, UUID> entry : userToGroupMap.entrySet()) {
            if (entry.getValue().equals(groupId)) {
                String username = entry.getKey();
                UUID userId = userIdMap.get(username);
                System.out.println(" - " + username + ": " + userId);
            }
        }
    }

}
