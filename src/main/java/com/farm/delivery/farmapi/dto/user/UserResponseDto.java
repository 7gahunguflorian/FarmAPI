package com.farm.delivery.farmapi.dto.user;

import com.farm.delivery.farmapi.model.User;

public record UserResponseDto(
    Long id,
    String name,
    String email,
    String username,
    User.Role role,
    String profileImageUrl
) {} 