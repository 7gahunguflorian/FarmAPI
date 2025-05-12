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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

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
    }

    public UserResponseDto register(UserRegistrationDto registrationDto) {
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
            
            User savedUser = userRepository.save(user);
            logger.info("User saved to database with ID: {}", savedUser.getId());
            
            return convertToUserResponseDto(savedUser);
        } catch (Exception e) {
            logger.error("Error during user registration: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to register user: " + e.getMessage());
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
        user.setProfileImageUrl(imageUrl);
        userRepository.save(user);
        return new UpdateProfileImageDto(imageUrl);
    }

    private UserResponseDto convertToUserResponseDto(User user) {
        return new UserResponseDto(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getUsername(),
            user.getRole(),
            user.getProfileImageUrl()
        );
    }
}
