/*
 * This source file was generated by the Gradle 'init' task
 */
package dev.nextchat.server;

import java.sql.*;

public class App {
    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/chatdb",
                "root", "aio2024");

            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery("SELECT * FROM user_account"); 

            while (resultSet.next()) {
                System.out.println(resultSet.getString("username") + ": " + resultSet.getString("user_password"));
            }
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
