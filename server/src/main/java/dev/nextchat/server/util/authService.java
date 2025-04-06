package dev.nextchat.server.util;

public class authService {
    // This class is responsible for authentication service in the Messages Server
    // It will be used to authenticate the user and get the session code
    // It will be used in the SendingThread class to send messages to the client
    // It will be used in the ReceivingThread class to receive messages from the client

    public String getUserId(String sessionCode) {
        // This method is responsible for getting the session code from the server

        return "12345"; // Example session code
    }
}
