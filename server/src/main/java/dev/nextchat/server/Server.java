package dev.nextchat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Server {
    private static final int THREAD_POOL_SIZE = 10;
    private static final int PORT = 5001;

    public static void main(String[] args) {
        SpringApplication.run(Server.class, args);

        try {
            startServer();
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }

    public static void startServer() throws IOException {
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
