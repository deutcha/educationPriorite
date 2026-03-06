package com.devpro.devlearningroadmapmanager.securities.configuration;

import com.devpro.devlearningroadmapmanager.securities.entities.User;
import com.devpro.devlearningroadmapmanager.securities.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "admin@education.com";

            if (userRepository.findByEmail(adminEmail) == null) {
                User admin = User.builder()
                        .username("admin")
                        .email(adminEmail)
                        .passwordHash(passwordEncoder.encode("Admin123!"))
                        .role("SUPER_ADMIN")
                        .build();

                userRepository.save(admin);
            }
        };
    }
}