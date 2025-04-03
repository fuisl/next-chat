package dev.nextchat.server.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

// This class is responsible for sending thread in the Message Server

public class SendingThread implements Runnable {
    
    private Socket s;
    SendingThread(Socket Cl)
    {
        s = Cl;
    }

    //temp data type for sending thread testing
    private static class Client {
        public String tempMessage; // Example message to send
        public String tempGroupId; // Example group ID to send the message to
        public String tempSessionCode; // Example session code to send the message to

        public Client() {
            this.tempMessage = "Hello from sending thread!";
            this.tempGroupId = "12345";
            this.tempSessionCode = "11111";
        }
    }

    private static void onMessageSendingRequest(Socket s, Client clientData) {
        // This method is responsible for sending message to the client
        // It will be called in the run() method of the SendingThread class

        String tempMessage = clientData.tempMessage; // Example message to send {if tempMessage is null => client want to view messages from the groupId}
        String tempGroupId = clientData.tempGroupId; // Example group ID to send the message to'
        String tempSessionCode = clientData.tempSessionCode; // Example session code to send the message to
        
        DataOutputStream gui=null;
        try {
            gui = new DataOutputStream(s.getOutputStream());
            while(true)
            {
                String tam2 = tempSessionCode + " " + tempMessage + " " + tempGroupId; // Example message to send
                gui.writeUTF(tam2);
                if(tam2.equals("00000 bye")) //sample notify user not online anymore =))))
                {
                    gui.close();
                    s.close();
                }
        
            }              
        } catch (IOException ex) {
            
        }
    }
    
    @Override
    public void run()
    {
        // TODO: Listen to the message and sessionCode from the client

        Client clientData = new Client(); // Example client data to send
        onMessageSendingRequest(s, clientData);      
    }
}
