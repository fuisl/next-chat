package dev.nextchat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.JSONObject;

public class ClientHandler implements Runnable {
    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        System.out.printf("ClientHandlerThread started for %s%n", socket.getInetAddress());

        try (
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);
        ) {
            // Step 1: Perform Handshake
            writer.println("HELLO_CLIENT");  // Send handshake message
            String handshakeResponse = reader.readLine();  // Wait for response

            if (!"HELLO_SERVER".equals(handshakeResponse)) {
                System.out.println("Handshake failed. Closing connection.");
                return;
            }
            System.out.println("Handshake successful!");

            // Step 2: Continuous listening for messages
            while (true) {
                String jsonMessage = reader.readLine();  // Read full line

                if (jsonMessage == null) {
                    System.out.println("Client disconnected.");
                    break; // Stop the loop if client disconnects
                }

                jsonMessage = jsonMessage.trim();
                if (jsonMessage.isEmpty()) {
                    System.out.println("Received empty message, waiting...");
                    continue; // Ignore empty input
                }

                if ("exit".equalsIgnoreCase(jsonMessage)) {
                    System.out.println("Client requested to close the connection.");
                    break; // Exit loop on "exit"
                }

                System.out.printf("Received JSON from %s: %s%n", socket.getInetAddress(), jsonMessage);

                // Step 3: Unpack JSON
                try {
                    JSONObject jsonObject = new JSONObject(jsonMessage);
                    String username = jsonObject.getString("username");
                    String password = jsonObject.getString("passwd");

                    System.out.println("Extracted Username: " + username);
                    System.out.println("Extracted Password: " + password);

                    // Step 4: Send acknowledgment
                    JSONObject responseJson = new JSONObject();
                    responseJson.put("message", "Server received credentials successfully");
                    writer.println(responseJson.toString());  // Send response
                } catch (Exception e) {
                    System.out.println("Invalid JSON received: " + jsonMessage);
                }
            }
        } catch (IOException e) {
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
