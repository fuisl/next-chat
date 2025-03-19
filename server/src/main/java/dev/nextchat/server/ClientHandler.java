package dev.nextchat.server;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        System.out.printf("ClientHandlerThread started for %s%n", socket.getInetAddress());

        try (BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {

            // Send welcome message
            output.println("Welcome to the server!");

            String clientInput;
            while ((clientInput = input.readLine()) != null) {
                System.out.printf("Received from %s: %s%n", socket.getInetAddress(), clientInput);
            }
        } catch (Exception e) {
            System.out.printf("Exception occurred for %s: %s%n", socket.getInetAddress(), e.getMessage());
        } finally {
            try {
                socket.close();
                System.out.printf("Connection closed for %s%n", socket.getInetAddress());
            } catch (IOException e) {
                System.out.printf("Error closing connection for %s: %s%n", socket.getInetAddress(), e.getMessage());
            }
        }
    }
}
