package com.farm.delivery.farmapi.service;

import com.farm.delivery.farmapi.dto.UserDTOs.*;
import com.farm.delivery.farmapi.model.User;
import com.farm.delivery.farmapi.repository.UserRepository;
import com.farm.delivery.farmapi.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final Path rootLocation = Paths.get("uploads/profile-images");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;

        // Create upload directory if it doesn't exist
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            logger.error("Could not initialize storage", e);
        }
    }

    // Existing registration method
    public UserResponseDto register(UserRegistrationDto registrationDto) {
        return registerWithImage(registrationDto, null);
    }

    // New method for registration with image
    @Transactional
    public UserResponseDto registerWithImage(UserRegistrationDto registrationDto, MultipartFile file) {
        logger.debug("Attempting to register user with email: {}", registrationDto.email());

        // Check if user exists by email or username
        boolean emailExists = userRepository.existsByEmail(registrationDto.email());
        boolean usernameExists = userRepository.existsByUsername(registrationDto.username());

        if (emailExists) {
            logger.warn("Registration failed: Email already in use: {}", registrationDto.email());
            throw new RuntimeException("Email already in use");
        }

        if (usernameExists) {
            logger.warn("Registration failed: Username already in use: {}", registrationDto.username());
            throw new RuntimeException("Username already in use");
        }

        // Only allow FARMER and CLIENT roles for self-registration
        if (registrationDto.role() == User.Role.ADMIN) {
            logger.warn("Registration failed: Admin registration not allowed");
            throw new RuntimeException("Admin registration not allowed");
        }

        try {
            User user = new User();
            user.setName(registrationDto.name());
            user.setEmail(registrationDto.email());
            user.setUsername(registrationDto.username());

            String encodedPassword = passwordEncoder.encode(registrationDto.password());
            user.setPassword(encodedPassword);

            user.setRole(registrationDto.role());

            // Handle profile image if provided
            if (file != null && !file.isEmpty()) {
                String imageUrl = storeProfileImage(file);
                user.setProfileImageUrl(imageUrl);
            }

            User savedUser = userRepository.save(user);
            logger.info("User registered successfully with ID: {}", savedUser.getId());

            return convertToUserResponseDto(savedUser);
        } catch (Exception e) {
            logger.error("Error during user registration: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to register user: " + e.getMessage());
        }
    }

    // Helper method to store profile image
    private String storeProfileImage(MultipartFile file) {
        try {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path destinationFile = rootLocation.resolve(Paths.get(filename))
                    .normalize()
                    .toAbsolutePath();

            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
            return filename; // Return just the filename
        } catch (IOException e) {
            logger.error("Failed to store profile image", e);
            throw new RuntimeException("Failed to store profile image: " + e.getMessage());
        }
    }

    public AuthResponseDto login(UserLoginDto loginDto) {
        logger.debug("Attempting to login user with username: {}", loginDto.username());

        try {
            // First, check if user exists by username
            Optional<User> userOpt = userRepository.findByUsername(loginDto.username());
            logger.debug("User lookup result for username {}: {}", loginDto.username(), userOpt.isPresent());

            if (userOpt.isEmpty()) {
                logger.warn("Login failed: User not found with username: {}", loginDto.username());
                throw new BadCredentialsException("Invalid username or password");
            }

            // Try to authenticate
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.username(), loginDto.password()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.debug("Authentication successful for user: {}", loginDto.username());

            final UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            final String token = jwtTokenProvider.generateToken(authentication);
            logger.debug("JWT token generated successfully for user: {}", loginDto.username());

            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return new AuthResponseDto(token, convertToUserResponseDto(user));
        } catch (BadCredentialsException e) {
            logger.warn("Login failed: Invalid credentials for user: {}", loginDto.username());
            throw new BadCredentialsException("Invalid username or password");
        } catch (Exception e) {
            logger.error("Login failed with unexpected error for user: {}", loginDto.username(), e);
            throw new RuntimeException("Login failed: " + e.getMessage());
        }
    }

    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToUserResponseDto)
                .collect(Collectors.toList());
    }

    public List<UserResponseDto> getUsersByRole(User.Role role) {
        return userRepository.findByRole(role).stream()
                .map(this::convertToUserResponseDto)
                .collect(Collectors.toList());
    }

    public Optional<UserResponseDto> getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToUserResponseDto);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }

    @Transactional
    public UpdateProfileImageDto updateProfileImage(String username, String imageUrl) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Store just the filename in the database
        String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        user.setProfileImageUrl(filename);
        userRepository.save(user);
        
        return new UpdateProfileImageDto(filename);
    }

    private UserResponseDto convertToUserResponseDto(User user) {
        String profileImageUrl = user.getProfileImageUrl();
        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            // If the URL doesn't start with http, it's a local file
            if (!profileImageUrl.startsWith("http")) {
                // Make sure we're using the correct path format
                if (!profileImageUrl.startsWith("/")) {
                    profileImageUrl = "/profile-images/" + profileImageUrl;
                }
            }
        }
        
        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getUsername(),
                user.getRole(),
                profileImageUrl);
    }
}
