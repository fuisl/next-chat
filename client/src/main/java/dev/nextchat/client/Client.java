package dev.nextchat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 5001;

    public static void main(String[] args) {
        SpringApplication.run(Client.class, args);
        
        try (
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))
        ) {
            // Step 1: Perform Handshake
            System.out.println("Waiting for server handshake...");
            String serverMessage = reader.readLine();
            
            if (serverMessage == null || !"HELLO_CLIENT".equals(serverMessage)) {
                System.out.println("Unexpected handshake message or connection closed: " + serverMessage);
                return;
            }

            System.out.println("Sending handshake response...");
            writer.println("HELLO_SERVER");

            System.out.println("Handshake completed! You can now enter credentials.");

            // Step 2: Continuous communication loop
            while (true) {
                System.out.print("Enter username (or type 'exit' to quit): ");
                

                String username = consoleInput.readLine().trim();
                if (username.isEmpty()) {
                    System.out.println("Username cannot be empty. Try again.");
                    continue;
                }
                if ("exit".equalsIgnoreCase(username)) {
                    System.out.println("Sending exit command to server...");
                    writer.println("exit");
                    break;
                }

                System.out.print("Enter password: ");
                String password = consoleInput.readLine().trim();
                if (password.isEmpty()) {
                    System.out.println("Password cannot be empty. Try again.");
                    continue;
                }

                // Step 3: Send JSON payload
                JSONObject payload = new JSONObject();
                payload.put("username", username);
                payload.put("passwd", password);

                System.out.println("Sending JSON payload to server...");
                writer.println(payload.toString());

                // Step 4: Read server response
                String response = reader.readLine();
                if (response == null) {
                    System.out.println("Server closed the connection.");
                    break;
                }
                System.out.println("Server Response: " + response);
                System.out.println("------------------------------");
            }

        } catch (IOException e) {
            System.err.println("Error in client communication: " + e.getMessage());
        }
    }
}
