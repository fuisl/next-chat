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

// import dev.nextchat.server.controllers.MessageController;

public class ClientHandler implements Runnable {
    private final Socket socket;

    // Get Session ID to identify the user
    // private static final userID getSession (){
    //     return new userID();
    // }

    //temp data type for sending thread testing
    // private static class Client {
    //     public String tempMessage; // Example message to send
    //     public String tempGroupId; // Example group ID to send the message to
    //     public String tempSessionCode; // Example session code to send the message to

    //     public Client() {
    //         this.tempMessage = "Hello from sending thread!";
    //         this.tempGroupId = "12345";
    //         this.tempSessionCode = "11111";
    //     }
    // }

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
            // // Send welcome message
            // output.println("Welcome to the server!");

            // String clientInput;
            // while ((clientInput = input.readLine()) != null) {
            //     System.out.printf("Received from %s: %s%n", socket.getInetAddress(), clientInput);

            //     switch (clientInput) {
            //         // case "authenticate":
            //         //     // Simulate user authentication
            //         //     output.println("Please enter your user ID:");
            //         //     String userId = input.readLine();
            //         //     // Simulate successful authentication
            //         //     output.println("Authenticated as " + userId);
            //         //     break;

            //         case "onMessageSendingRequest":
            //             // Simulate getting user ID
            //             // Example client data to send
            //             new MessageController(socket);
            //             break;

            //         default:
            //             output.println("Unknown command: " + clientInput);
            //     }
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
