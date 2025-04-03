package dev.nextchat.server;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MessageServer {
    private static final int NTHREADS = 100; //maximum threads for this program
	private static final ExecutorService exec = Executors.newFixedThreadPool(NTHREADS); 

    //todo take info from user
        
	public static void main(String[] args) throws IOException {
		ServerSocket socket = new ServerSocket(1234);     
		while (true) { 
			Socket connection = socket.accept();
			ClientHandler each_client = new ClientHandler(connection);
			exec.execute(each_client); 
		} 
	} 

    
    
    
}

