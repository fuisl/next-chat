
package dev.nextchat.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 5001;

    public static void main(String[] args) {
        SpringApplication.run(Client.class, args);
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
                Scanner scanner = new Scanner(System.in)) {

            // Step 1: Perform Handshake
            byte[] buffer = new byte[1024];
            int bytesRead = inputStream.read(buffer);
            String serverMessage = new String(buffer, 0, bytesRead, "UTF-8").trim();

            if (!"HELLO_CLIENT".equals(serverMessage)) {
                System.out.println("Unexpected handshake message: " + serverMessage);
                return;
            }
            outputStream.write("HELLO_SERVER\n".getBytes("UTF-8"));
            outputStream.flush();
            System.out.println("Handshake completed!");

            // Step 2: Continuous communication
            while (true) {
                System.out.print("Enter username (or type 'exit' to quit): ");
                String username = scanner.nextLine();

                if ("exit".equalsIgnoreCase(username)) {
                    outputStream.write("exit".getBytes("UTF-8"));
                    outputStream.flush();
                    break; // Exit the loop
                }

                System.out.print("Enter password: ");
                String password = scanner.nextLine();

                // Step 3: Send JSON payload
                JSONObject payload = createJsonPayload(username, password);
                sendPayload(outputStream, payload);

                // Step 4: Read server response
                bytesRead = inputStream.read(buffer);
                if (bytesRead != -1) {
                    String response = new String(buffer, 0, bytesRead, "UTF-8");
                    System.out.println("Server Response: " + response);
                }

                System.out.println("------------------------------");
            }

        } catch (IOException e) {
            System.err.println("Error in client communication: " + e.getMessage());
        }
    }

    // Function to create JSON payload
    private static JSONObject createJsonPayload(String username, String password) {
        JSONObject json = new JSONObject();
        json.put("username", username);
        json.put("passwd", password);
        return json;
    }

    // Function to send JSON payload
    private static void sendPayload(OutputStream outputStream, JSONObject json) throws IOException {
        byte[] jsonBytes = json.toString().getBytes("UTF-8");
        outputStream.write(jsonBytes);
        outputStream.flush();
        System.out.println("Payload sent: " + json.toString());
    }
}
