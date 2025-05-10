package com.farm.delivery.farmapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AuthDTOs {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequestDto {
        private String username;
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResponseDto {
        private String token;
        private String username;
        private String role;
    }
} 