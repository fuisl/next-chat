//package dev.nextchat.client.database;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import dev.nextchat.client.models.User;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//public class UserDatabase {
//    private static final File USER_FILE = new File("src/main/resources/db/users.json");
//    private static final ObjectMapper mapper = new ObjectMapper();
//
//    public static List<User> loadUsers() throws IOException {
//        if (!USER_FILE.exists() || USER_FILE.length() == 0) {
//            return new ArrayList<>();
//        }
//        return mapper.readValue(USER_FILE, new TypeReference<>() {});
//    }
//
//
//    public static boolean authenticate(String username, String password) {
//        try {
//            return loadUsers().stream()
//                    .anyMatch(u -> u.getUsername().equals(username) && u.getPassword().equals(password));
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public static boolean userExists(String username) throws IOException {
//        return loadUsers().stream().anyMatch(u -> u.getUsername().equals(username));
//    }
//
//    public static void registerUser(User user) throws IOException {
//        List<User> users = loadUsers();
//        users.add(user);
//        mapper.writerWithDefaultPrettyPrinter().writeValue(USER_FILE, users);
//    }
//}
