package com.farm.delivery.farmapi.service;

import com.farm.delivery.farmapi.dto.user.*;
import com.farm.delivery.farmapi.exception.NonAdminAccessException;
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
    public String storeProfileImage(MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file");
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("Only image files are allowed");
            }

            // Create unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String filename = UUID.randomUUID().toString() + extension;

            // Ensure directory exists
            Files.createDirectories(rootLocation);

            // Store file
            Path destinationFile = rootLocation.resolve(filename)
                    .normalize()
                    .toAbsolutePath();

            // Validate path
            if (!destinationFile.getParent().equals(rootLocation.toAbsolutePath())) {
                throw new RuntimeException("Cannot store file outside current directory");
            }

            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
            return filename;
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

            User user = userOpt.get();
            
            // Check if the request is from web (has web flag) and user is not admin
            if (loginDto.isWeb() && user.getRole() != User.Role.ADMIN) {
                logger.warn("Web access denied for non-admin user: {}", loginDto.username());
                throw new NonAdminAccessException("Access denied. Please use the mobile app.");
            }

            // Try to authenticate
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.username(), loginDto.password()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.debug("Authentication successful for user: {}", loginDto.username());

            final UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            final String token = jwtTokenProvider.generateToken(authentication);
            logger.debug("JWT token generated successfully for user: {}", loginDto.username());

            return new AuthResponseDto(token, convertToUserResponseDto(user));
        } catch (BadCredentialsException e) {
            logger.warn("Login failed: Invalid credentials for user: {}", loginDto.username());
            throw new BadCredentialsException("Invalid username or password");
        } catch (NonAdminAccessException e) {
            logger.warn("Web access denied: {}", e.getMessage());
            throw e;
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

    public UserResponseDto convertToUserResponseDto(User user) {
        String profileImageUrl = user.getProfileImageUrl();
        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            // If the URL doesn't start with http, it's a local file
            if (!profileImageUrl.startsWith("http")) {
                // Construct the full URL using the server's base URL
                profileImageUrl = String.format("http://localhost:8180/profile-images/%s", profileImageUrl);
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

    @Transactional(readOnly = true)
    public UserStatsDto getUserStats() {
        return new UserStatsDto(
            countByRole(User.Role.FARMER),
            countByRole(User.Role.CLIENT)
        );
    }

    public long countByRole(User.Role role) {
        return userRepository.countByRole(role);
    }
}
