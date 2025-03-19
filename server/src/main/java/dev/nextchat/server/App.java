/*
 * This source file was generated by the Gradle 'init' task
 */
package dev.nextchat.server;

import java.sql.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class App {
    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        
        ServerSocket ss = new ServerSocket(1234);
        Socket con = ss.accept();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream())); 
        PrintWriter out = new PrintWriter(con.getOutputStream(), true);

        String choice = in.readLine().trim(); 
        String username = in.readLine().trim();
        String password = in.readLine().trim();

        MessageDigest md = MessageDigest.getInstance("SHA-256"); 
        md.update(password.getBytes());
        byte[] digest = md.digest();
        String hashedPassword = String.format("%064x", new BigInteger(1, digest));

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/chatdb",
                "root", "aio2024");
            
            System.out.println("Connected to database....");

            if (choice.equals("2")) {
                try {
                    String query = "INSERT INTO user_account (username, user_password) VALUES (?, ?)";

                    PreparedStatement statement = connection.prepareStatement(query);

                    statement.setString(1, username);
                    statement.setString(2, hashedPassword);
                    statement.executeUpdate();

                    out.println("Account created successfully");
                } 
                catch (SQLIntegrityConstraintViolationException e) {
                    out.println("Username already exists");
                }
            }
            else if (choice.equals("1")) {
                String query = "SELECT user_password FROM user_account WHERE username = ?";

                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, username);

                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    if (resultSet.getString("user_password").equals(hashedPassword)) {
                        out.println("Login successful");
                    }
                    else {
                        out.println("Incorrect password");
                    }
                }
                else {
                    out.println("Username does not exists");
                }
            }
             
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
