package com.farm.delivery.farmapi.controller;

import com.farm.delivery.farmapi.dto.UserDTOs.AuthResponseDto;
import com.farm.delivery.farmapi.dto.UserDTOs.UserLoginDto;
import com.farm.delivery.farmapi.dto.UserDTOs.UserRegistrationDto;
import com.farm.delivery.farmapi.dto.UserDTOs.UserResponseDto;
import com.farm.delivery.farmapi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRegistrationDto registrationDto) {
        return ResponseEntity.ok(userService.register(registrationDto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody UserLoginDto loginDto) {
        return ResponseEntity.ok(userService.login(loginDto));
    }
}
