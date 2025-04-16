package dev.nextchat.server;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class Authenticator {
    private static Authenticator instance; 
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private UserRepository userRepository;

    public static Authenticator getInstance() {
        if (instance == null) {
            instance = new Authenticator();
        }
        return instance;
    }

    public boolean signIn(Credential cred) {
        Optional<User> userOpt = userRepository.findByUsername(cred.getUsername());
        return userOpt.isPresent() && passwordEncoder.matches(cred.getHashedPassword(), userOpt.get().getPassword());
    }

    @Transactional
    public boolean signUp(Credential cred) {
        if (userRepository.existsByUsername(cred.getUsername())) {
            return false;
        }

        User newUser = new User(cred.getUsername(), cred.getHashedPassword());
        userRepository.save(newUser);
        return true;
    }
}
