package dev.nextchat.server.auth.service;

import dev.nextchat.server.auth.model.User;
import dev.nextchat.server.auth.model.Credential;
import dev.nextchat.server.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class Authenticator {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public Authenticator(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public boolean signIn(Credential credential) {
        Optional<User> userOpt = userRepository.findByUsername(credential.getUsername());
        return userOpt.isPresent() &&
                passwordEncoder.matches(credential.getRawPassword(), userOpt.get().getPassword());
    }

    @Transactional
    public int updateLastOnlineTimeStamp(Instant time, UUID userId) {
        return userRepository.setLastOnlineTimeStamp(time, userId);
    }

    @Transactional
    public boolean signUp(Credential credential) {
        if (userRepository.existsByUsername(credential.getUsername())) {
            return false;
        }
        String hashedPassword = passwordEncoder.encode(credential.getRawPassword());
        User user = new User(credential.getUsername(), hashedPassword);
        userRepository.save(user);
        return true;
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> getUserByUserId(UUID id) {
        return userRepository.findById(id);
    }

    public UUID getUserIdByUsername(String username) {
        return getUserByUsername(username)
                .map(User::getId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }
}
