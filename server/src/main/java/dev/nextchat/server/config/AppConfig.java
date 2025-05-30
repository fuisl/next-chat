package dev.nextchat.server.config;

import dev.nextchat.server.protocol.*;
import dev.nextchat.server.protocol.registry.CommandFactoryRegistry;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {
        "dev.nextchat.server.auth.repository",
        "dev.nextchat.server.session.repository",
        "dev.nextchat.server.group.repository"
})
@EnableMongoRepositories(basePackages = {
        "dev.nextchat.server.messaging.repository"
})
public class AppConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ProtocolDecoder protocolDecoder(CommandFactoryRegistry registry) {
        return new ProtocolDecoder(registry);
    }
}
