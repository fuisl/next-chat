package dev.nextchat.server;

import jakarta.annotation.Generated;
import jakarta.persistence.*;

@Entity 
@Table(name = "user_account")
public class User {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @Column(unique = true, nullable = false)
    private String username; 

    @Column(nullable = false)
    private String password; 

    // Getters and Setters 
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
