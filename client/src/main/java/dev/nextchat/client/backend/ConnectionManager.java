package dev.nextchat.client.backend;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ConnectionManager {
    private final String SERVER_ADDRESS = "localhost";
    private final int SERVER_PORT = 5001;
    private BufferedReader reader;
    private PrintWriter writer;
    private Socket socket;

    public ConnectionManager() {
    };

    public String init() {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            // Step 1: Perform Handshake
            String serverMessage = reader.readLine();

            if (serverMessage == null || !"HELLO_CLIENT".equals(serverMessage)) {
                System.out
                        .println("Unexpected handshake protocol or connection error. Error message: " + serverMessage);
                return "FAIL";
            }

            System.out.println("Sending handshake response...");
            writer.println("HELLO_SERVER");

            System.out.println("Waiting for server handshake...");
            serverMessage = reader.readLine();

            if (serverMessage == null || !"WELCOME".equals(serverMessage)) {
                System.out.println(serverMessage);
                return "FAIL";
            }

            System.out.println("Handshake completed!");

            // set connection parameters after successful handshake
            this.reader = reader;
            this.writer = writer;
            this.socket = socket;
        } catch (IOException e) {
            System.err.println("Error in client communication: " + e.getMessage());
        }

        return "SUCCESS";
    }

    public BufferedReader getReader() {
        return this.reader;
    }

    public PrintWriter getWriter() {
        return this.writer;
    }

}
