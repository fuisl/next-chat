// Directory: dev/nextchat/server/auth/service

package dev.nextchat.server.auth.service;

import dev.nextchat.server.auth.model.User;
import dev.nextchat.server.auth.model.Credential;
import dev.nextchat.server.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class Authenticator {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public Authenticator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean signIn(Credential credential) {
        Optional<User> userOpt = userRepository.findByUsername(credential.getUsername());
        return userOpt.isPresent() &&
                passwordEncoder.matches(credential.getRawPassword(), userOpt.get().getPassword());
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
}