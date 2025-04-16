package dev.nextchat.server.net;

import org.json.JSONObject;

import dev.nextchat.server.auth.service.Authenticator;
import dev.nextchat.server.context.SpringContext;
import dev.nextchat.server.protocol.*;

import java.io.*;
import java.net.Socket;


public class ClientHandler implements Runnable {
    private final Authenticator authenticator=SpringContext.getBean(Authenticator.class);
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
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true)) {
            // Step 1: Handshake
            writer.println("HELLO_CLIENT");
            String handshakeResponse = reader.readLine();

            if (!"HELLO_SERVER".equals(handshakeResponse)) {
                System.out.println("Handshake failed. Closing connection.");
                return;
            }

            System.out.println("Handshake successful.");
            writer.println("WELCOME");

            // Step 2: JSON message loop
            while (true) {
                String jsonMessage = reader.readLine();

                if (jsonMessage == null) {
                    System.out.println("Client disconnected.");
                    break;
                }

                jsonMessage = jsonMessage.trim();
                if (jsonMessage.isEmpty())
                    continue;

                if ("exit".equalsIgnoreCase(jsonMessage)) {
                    System.out.println("Client requested termination.");
                    break;
                }

                System.out.printf("Received JSON from %s: %s%n", socket.getInetAddress(), jsonMessage);

                try {
                    Command command = ProtocolDecoder.parse(jsonMessage);
                    JSONObject response = command.execute(authenticator);
                    writer.println(response.toString());

                } catch (Exception e) {
                    JSONObject error = new JSONObject();
                    error.put("type", "error");
                    error.put("message", "Failed to parse command: " + e.getMessage());
                    writer.println(error.toString());
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
