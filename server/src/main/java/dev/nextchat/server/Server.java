package dev.nextchat.server;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int THREAD_POOL_SIZE = 10;
    private static final int PORT = 5001;

    public static void main(String[] args) throws IOException {
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE, new ClientThreadFactory());
        ServerSocket serverSocket = new ServerSocket(PORT);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("Shutting down server...");
                serverSocket.close();
                threadPool.shutdown();
            } catch (IOException e) {
                System.err.println("Error while shutting down server: " + e.getMessage());
            }
        }));

        try {
            System.out.println("Server is listening on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.printf("New client connected: %s:%d%n", clientSocket.getInetAddress(),
                        clientSocket.getPort());

                threadPool.execute(new ClientHandler(clientSocket));
            }
        } finally {
            serverSocket.close();
            threadPool.shutdown();
        }
    }
}
