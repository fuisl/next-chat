package dev.nextchat.client.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;


public class ConnectionManager {
    private final String SERVER_ADDRESS = "localhost";
    private final int SERVER_PORT = 1234;
    private String TOKEN = "some_token";
    private BufferedReader reader;
    private PrintWriter writer;
    private Socket socket;

    public ConnectionManager(){};

    public String init() {
        try (
            BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))
        ) {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            System.out.println("Sending handshake response...");
            writer.println("HELLO_SERVER");

            // Step 1: Perform Handshake
            System.out.println("Waiting for server handshake...");
            String serverMessage = reader.readLine();
            
            if (serverMessage == null || !"HELLO_CLIENT".equals(serverMessage)) {
                System.out.println("Unexpected handshake message or connection closed: " + serverMessage);
                return "FAIL";
            }

            System.out.println("Handshake completed! You can now enter credentials.");

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