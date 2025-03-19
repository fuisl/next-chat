package dev.nextchat.server;

import java.io.*;
import java.net.*;
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
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
        ) {
            // Step 1: Perform Handshake
            outputStream.write("HELLO_CLIENT\n".getBytes("UTF-8"));
            outputStream.flush();

            byte[] buffer = new byte[1024];

            // Expect handshake from client
            int bytesRead = inputStream.read(buffer);
            String handshakeResponse = new String(buffer, 0, bytesRead, "UTF-8").trim();

            if (!"HELLO_SERVER".equals(handshakeResponse)) {
                System.out.println("Handshake failed. Closing connection.");
                socket.close();
                return;
            }
            System.out.println("Handshake successful!");

            // Step 2: Continuous listening for messages
            while (true) {
                bytesRead = inputStream.read(buffer);
                if (bytesRead == -1) break; // Client disconnected

                byteArrayOutputStream.write(buffer, 0, bytesRead);
                String jsonMessage = byteArrayOutputStream.toString("UTF-8").trim();
                byteArrayOutputStream.reset(); // Clear buffer after processing

                if ("exit".equalsIgnoreCase(jsonMessage)) {
                    System.out.println("Client requested to close the connection.");
                    break;
                }

                System.out.printf("Received JSON from %s: %s%n", socket.getInetAddress(), jsonMessage);

                // Step 3: Unpack JSON and extract credentials
                JSONObject jsonObject = new JSONObject(jsonMessage);
                String username = jsonObject.getString("username");
                String password = jsonObject.getString("passwd");

                System.out.println("Extracted Username: " + username);
                System.out.println("Extracted Password: " + password);

                // Step 4: Send acknowledgment
                JSONObject responseJson = new JSONObject();
                responseJson.put("message", "Server received credentials successfully");
                outputStream.write(responseJson.toString().getBytes("UTF-8"));
                outputStream.flush();
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
