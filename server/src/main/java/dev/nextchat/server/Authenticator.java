package dev.nextchat.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLException;

public final class Authenticator {
    private static Authenticator instance;
    private Connection connection;
    private PreparedStatement statement; 

    private Authenticator() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/chatdb",
                "root", "aio2024");
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    
    } 

    public static Authenticator getInstance() {
        if (instance == null) {
            instance = new Authenticator();
        }
        return instance;
    } 

    public boolean signIn(Credential cred) { 
        String query = "SELECT user_password FROM user_account WHERE username = ?";

        try {
            statement = connection.prepareStatement(query); 
            statement.setString(1, cred.getUsername()); 

            ResultSet resultSet = statement.executeQuery(); 

            if (resultSet.next() && resultSet.getString("user_password").equals(cred.getHashedPassword())) {
                return true; 
            }
        } 
        catch (Exception e) {
            e.printStackTrace();
        }

        return false; 
    }

    public boolean signUp(Credential cred) throws SQLException {
        String query = "INSERT INTO user_account (username, user_password) VALUES (?, ?)";

        try {
            statement = connection.prepareStatement(query);
            statement.setString(1, cred.getUsername());
            statement.setString(2, cred.getHashedPassword());

            statement.executeUpdate();

            return true; 
        } 
        catch (SQLIntegrityConstraintViolationException e) {
            e.printStackTrace();
        }

        return false; 

    }

}
