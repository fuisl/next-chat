package dev.nextchat.server.net;

import dev.nextchat.server.auth.service.Authenticator;
import dev.nextchat.server.session.service.SessionService;
import dev.nextchat.server.group.service.GroupService;
import dev.nextchat.server.protocol.*;
import dev.nextchat.server.protocol.impl.LoginCommand;
import dev.nextchat.server.shared.dto.SessionToken;

import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final ProtocolDecoder decoder;
    private final Authenticator authenticator;
    private final SessionService sessionService;
    private final GroupService groupService;
    private SessionToken sessionToken;

    public ClientHandler(Socket socket,
            ProtocolDecoder decoder,
            Authenticator authenticator,
            SessionService sessionService,
            GroupService groupService) {
        this.socket = socket;
        this.decoder = decoder;
        this.authenticator = authenticator;
        this.sessionService = sessionService;
        this.groupService = groupService;
    }

    @Override
    public void run() {
        System.out.printf("ClientHandler started for %s%n", socket.getInetAddress());

        try (
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true)) {

            writer.println("HELLO_CLIENT");
            String handshakeResponse = reader.readLine();

            if (!"HELLO_SERVER".equals(handshakeResponse)) {
                System.out.println("Handshake failed. Closing connection.");
                return;
            }

            System.out.println("Handshake successful.");
            writer.println("WELCOME");

            while (true) {
                String jsonMessage = reader.readLine();
                if (jsonMessage == null || jsonMessage.isBlank())
                    break;
                if ("exit".equalsIgnoreCase(jsonMessage))
                    break;

                try {
                    Command command = decoder.parse(jsonMessage);
                    UUID userId = sessionToken != null ? sessionToken.getUserId() : null;
                    CommandContext context = new CommandContext(authenticator, sessionService, groupService, userId);

                    JSONObject response = command.execute(context);
                    writer.println(response.toString());

                    if (command instanceof LoginCommand loginCmd) {
                        this.sessionToken = loginCmd.getSessionToken();
                        System.out.printf("Session %s registered for user %s%n",
                                sessionToken.getToken(), sessionToken.getUserId());
                    }

                } catch (Exception e) {
                    JSONObject error = new JSONObject();
                    error.put("type", "error");
                    error.put("message", "Failed to parse/execute command: " + e.getMessage());
                    writer.println(error.toString());
                }
            }

        } catch (IOException e) {
            System.err.printf("I/O error with client %s: %s%n", socket.getInetAddress(), e.getMessage());
        } finally {
            try {
                if (sessionToken != null) {
                    sessionService.removeSession(sessionToken.getToken());
                    System.out.printf("Session %s removed for %s%n", sessionToken.getToken(), socket.getInetAddress());
                }
                socket.close();
                System.out.printf("Connection closed for %s%n", socket.getInetAddress());
            } catch (IOException e) {
                System.err.printf("Error closing socket for %s: %s%n", socket.getInetAddress(), e.getMessage());
            }
        }
    }
}
