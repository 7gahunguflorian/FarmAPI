package com.farm.delivery.farmapi.dto.user;

import jakarta.validation.constraints.NotBlank;

public record UserLoginDto(
    @NotBlank(message = "Username is required")
    String username,
    
    @NotBlank(message = "Password is required")
    String password,
    
    boolean isWeb
) {} 