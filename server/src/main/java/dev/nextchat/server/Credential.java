package dev.nextchat.server;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Credential {
    private final String username; 
    private final String hashedPassword; 

    public Credential(String username, String password) {
        this.username = username;
        this.hashedPassword = this.hashPassword(password);
    }

    public String getUsername() {
        return username;
    } 

    public String getHashedPassword() {
        return hashedPassword;
    } 

    public String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256"); 
            md.update(password.getBytes());
            byte[] digest = md.digest();
            return String.format("%064x", new BigInteger(1, digest));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password; 
        }
    }
}
