package dev.nextchat.server.net;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ServerLauncher implements CommandLineRunner {
    private static final int PORT = 5001;
    private static final int THREAD_POOL_SIZE = 64;

    private final ExecutorService threadPool = Executors.newFixedThreadPool(
        THREAD_POOL_SIZE, new ClientThreadFactory()
    );

    @Override
    public void run(String... args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.printf("✅ MsgServer is listening on port %d%n", PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.printf("📥 New connection from %s:%d%n",
                        socket.getInetAddress(), socket.getPort());

                threadPool.execute(new ClientHandler(socket));
            }

        } catch (Exception e) {
            System.err.println("🔥 Server failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }
}
