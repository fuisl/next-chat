package dev.nextchat.server;

public interface SessionInterface {
    
    boolean isOnline(String cookieID);

    boolean validateID(String cookieID);

    void informOnline(String cookieID); 
}
