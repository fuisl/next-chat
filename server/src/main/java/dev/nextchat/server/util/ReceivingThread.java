package dev.nextchat.server.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

// This class is responsible for receiving thread in the Messages Server

public class ReceivingThread implements Runnable {
    
    Socket s;
    ReceivingThread(Socket cl)
    {
        s = cl;
    }

    @Override
    public void run()
    {
        DataInputStream nhan;
        try {
            nhan = new DataInputStream(s.getInputStream());
            while(true)
                    {
            
                
                    String tam = nhan.readUTF(); // receive messages list from server
                    System.out.println(tam);
                
            
                    }           
        } catch (IOException ex) {
            
        }
        
    }
}
