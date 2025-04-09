package dev.nextchat.client.database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nextchat.client.models.Client;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class UserDatabase {
    private static final File USER_FILE = new File("src/main/resources/db/users.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<Client> loadUsers() throws IOException {
        return mapper.readValue(USER_FILE, new TypeReference<>() {});
    }

    public static boolean authenticate(String username, String password) {
        try {
            return loadUsers().stream()
                    .anyMatch(u -> u.getUsername().equals(username) && u.getPassword().equals(password));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean userExists(String username) throws IOException {
        return loadUsers().stream().anyMatch(u -> u.getUsername().equals(username));
    }

    public static void registerUser(Client user) throws IOException {
        List<Client> users = loadUsers();
        users.add(user);
        mapper.writerWithDefaultPrettyPrinter().writeValue(USER_FILE, users);
    }
}
