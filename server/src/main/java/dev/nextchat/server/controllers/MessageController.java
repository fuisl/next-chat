package dev.nextchat.server.controllers;

import java.net.Socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.util.concurrent.CopyOnWriteArrayList;

// This class is responsible for the message controller in the Messages Server

class SessionService {
    //sample class to request userId inside groupId in database
    public static CopyOnWriteArrayList<Socket> getUserId(String groupId) {
        // This method is responsible for getting the user InetAdress in the GroupchatId
        return new CopyOnWriteArrayList<>(); // Example list of user InetAddress
    }

    public static boolean isUserOnline(Socket userSocket) {
        // This method is responsible for checking if the user is online
        return true; // Example check for user online status
    }
}

//sample test class to simulate the client data
class Client {
    public String tempMessage; // Example message to send
    public String tempGroupId; // Example group ID to send the message to
    public String tempSessionCode; // Example session code to send the message to

    public Client() {
        this.tempMessage = "Hello from sending thread!";
        this.tempGroupId = "12345";
        this.tempSessionCode = "11111";
    }
}

public class MessageController {
    private Socket socket;
    private Client clientData;

    public MessageController(Socket socket) {
        this.socket = socket;
        this.clientData = new Client(); // Example client data
    }

    //@PostMapping("/messages")
    public void createMessage(Client clientData) {
        //This method is responsible for sending messages to the message database
    }

    public void sendMessage(Client clientData) {
        // This method is responsible for sending messages straight to the client
        
        CopyOnWriteArrayList<Socket> userIds = SessionService.getUserId(clientData.tempGroupId);// Get the user InetAdress from the group ID

        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            
            while(true)
            {
                String data = in.readUTF();
        
                for(int i = 0; i< userIds.size(); i++)
                {
                    if (SessionService.isUserOnline(userIds.get(i))) {
                        // Check if the user is online
                        Socket tempt = userIds.get(i);
                        
                        DataOutputStream out = new DataOutputStream(tempt.getOutputStream());
                        out.writeUTF(data);
                            
            
                    } else {}

                }           
            }
        } catch (IOException ex) {
            // Handle exception
            System.err.println("Error sending message in MessageController: " + ex.getMessage());
        }
       
        
    }

    public void run () {
        createMessage(this.clientData);
        sendMessage(this.clientData);
    }
}
