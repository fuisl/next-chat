package dev.nextchat.server;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Credential {
    private final String username; 
    private final String hashedPassword; 
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Credential(String username, String password) {
        this.username = username;
        this.hashedPassword = passwordEncoder.encode(password);
    }

    public String getUsername() {
        return username;
    } 

    public String getHashedPassword() {
        return hashedPassword;
    } 
}
