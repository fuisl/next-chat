package dev.nextchat.server.net;

import dev.nextchat.server.auth.repository.UserRepository;
import dev.nextchat.server.auth.service.Authenticator;
import dev.nextchat.server.group.service.GroupService;
import dev.nextchat.server.protocol.ProtocolDecoder;
import dev.nextchat.server.session.service.SessionService;
import dev.nextchat.server.messaging.service.RelayService;
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

    private final ProtocolDecoder decoder;
    private final Authenticator authenticator;
    private final SessionService sessionService;
    private final GroupService groupService;
    private final RelayService relayService;
    private final UserRepository userRepository;

    public ServerLauncher(
            ProtocolDecoder decoder,
            Authenticator authenticator,
            SessionService sessionService,
            GroupService groupService,
            RelayService relayService,
            UserRepository userRepository) {
        this.decoder = decoder;
        this.authenticator = authenticator;
        this.sessionService = sessionService;
        this.groupService = groupService;
        this.relayService = relayService;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.printf("✅ MsgServer is listening on port %d%n", PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.printf("📥 New connection from %s:%d%n",
                        socket.getInetAddress(), socket.getPort());

                ClientHandler handler = new ClientHandler(
                        socket,
                        decoder,
                        authenticator,
                        sessionService,
                        groupService,
                        relayService,
                        userRepository);

                threadPool.execute(handler);
            }

        } catch (Exception e) {
            System.err.println("🔥 Server failed: " + e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }
}
