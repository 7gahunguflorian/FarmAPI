package com.farm.delivery.farmapi.config;

import com.farm.delivery.farmapi.model.User;
import com.farm.delivery.farmapi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        logger.info("Initializing default users...");

        // Create admin user if not exists
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setName("Admin User");
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(User.Role.ADMIN);
            userRepository.save(admin);
            logger.info("Created admin user with username: admin and password: admin123");
        }

        // Create farmer user if not exists
        if (!userRepository.existsByUsername("farmer")) {
            User farmer = new User();
            farmer.setName("Farmer User");
            farmer.setUsername("farmer");
            farmer.setEmail("farmer@example.com");
            farmer.setPassword(passwordEncoder.encode("farmer123"));
            farmer.setRole(User.Role.FARMER);
            userRepository.save(farmer);
            logger.info("Created farmer user with username: farmer and password: farmer123");
        }

        // Create client user if not exists
        if (!userRepository.existsByUsername("client")) {
            User client = new User();
            client.setName("Client User");
            client.setUsername("client");
            client.setEmail("client@example.com");
            client.setPassword(passwordEncoder.encode("client123"));
            client.setRole(User.Role.CLIENT);
            userRepository.save(client);
            logger.info("Created client user with username: client and password: client123");
        }

        logger.info("Default users initialization completed");
    }
}