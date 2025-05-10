package com.farm.delivery.farmapi.dto;

import com.farm.delivery.farmapi.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserDTOs() {
    public record UserRegistrationDto(
            @NotBlank(message = "Name is required")
            String name,
            
            @NotBlank(message = "Username is required")
            @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
            String username,
            
            @NotBlank(message = "Email is required")
            @Email(message = "Invalid email format")
            String email,
            
            @NotBlank(message = "Password is required")
            @Size(min = 6, message = "Password must be at least 6 characters")
            String password,
            
            User.Role role
    ) {}

    public record UserLoginDto(
            @NotBlank(message = "Username is required")
            String username,
            
            @NotBlank(message = "Password is required")
            String password
    ) {}

    public record UserResponseDto(
            Long id,
            String name,
            String email,
            String username,
            User.Role role
    ) {}

    public record AuthResponseDto(
            String token,
            UserResponseDto user
    ) {}
}
