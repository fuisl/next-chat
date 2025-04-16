package dev.nextchat.server.net;

import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        System.out.printf("ClientHandler started for %s%n", socket.getInetAddress());

        try (
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true)
        ) {
            // Step 1: Handshake
            writer.println("HELLO_CLIENT");
            String handshakeResponse = reader.readLine();

            if (!"HELLO_SERVER".equals(handshakeResponse)) {
                System.out.println("Handshake failed. Closing connection.");
                return;
            }

            System.out.println("Handshake successful.");

            // Step 2: JSON message loop
            while (true) {
                String jsonMessage = reader.readLine();

                if (jsonMessage == null) {
                    System.out.println("Client disconnected.");
                    break;
                }

                jsonMessage = jsonMessage.trim();
                if (jsonMessage.isEmpty()) {
                    System.out.println("Empty message received. Skipping.");
                    continue;
                }

                if ("exit".equalsIgnoreCase(jsonMessage)) {
                    System.out.println("Client requested termination.");
                    break;
                }

                System.out.printf("Received JSON from %s: %s%n", socket.getInetAddress(), jsonMessage);

                // Step 3: Parse JSON
                try {
                    JSONObject jsonObject = new JSONObject(jsonMessage);

                    String username = jsonObject.getString("username");
                    String password = jsonObject.getString("passwd");

                    System.out.println("Parsed username: " + username);
                    System.out.println("Parsed password: " + password);

                    // Step 4: Acknowledge receipt
                    JSONObject responseJson = new JSONObject();
                    responseJson.put("message", "Server received credentials successfully");
                    writer.println(responseJson.toString());

                } catch (Exception e) {
                    System.out.println("Invalid JSON format: " + jsonMessage);
                }
            }

        } catch (IOException e) {
            System.out.printf("I/O error with %s: %s%n", socket.getInetAddress(), e.getMessage());
        } finally {
            try {
                socket.close();
                System.out.printf("Connection closed for %s%n", socket.getInetAddress());
            } catch (IOException e) {
                System.out.printf("Error closing socket for %s: %s%n", socket.getInetAddress(), e.getMessage());
            }
        }
    }
}
