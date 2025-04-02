package dev.nextchat.server;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class Authenticator {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public boolean signIn(Credential cred) {
        Optional<User> userOpt = userRepository.findByUsername(cred.getUsername());
        return userOpt.isPresent() && passwordEncoder.matches(cred.getHashedPassword(), userOpt.get().getPassword());
    }

    @Transactional
    public boolean signUp(Credential cred) {
        if (userRepository.existsByUsername(cred.getUsername())) {
            return false;
        }

        User newUser = new User(cred.getUsername(), passwordEncoder.encode(cred.getHashedPassword()));
        userRepository.save(newUser);
        return true;
    }
}
