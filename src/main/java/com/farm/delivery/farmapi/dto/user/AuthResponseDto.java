package com.farm.delivery.farmapi.dto.user;

public record AuthResponseDto(
    String token,
    UserResponseDto user
) {} 