package com.farm.delivery.farmapi.controller;

import com.farm.delivery.farmapi.dto.UserDTOs.AuthResponseDto;
import com.farm.delivery.farmapi.dto.UserDTOs.UserLoginDto;
import com.farm.delivery.farmapi.dto.UserDTOs.UserRegistrationDto;
import com.farm.delivery.farmapi.dto.UserDTOs.UserResponseDto;
import com.farm.delivery.farmapi.model.User;
import com.farm.delivery.farmapi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // Existing JSON registration endpoint
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRegistrationDto registrationDto) {
        return ResponseEntity.ok(userService.register(registrationDto));
    }

    // New multipart registration endpoint
    @PostMapping(value = "/register", consumes = { "multipart/form-data" })
    public ResponseEntity<UserResponseDto> registerWithImage(
            @RequestPart("name") String name,
            @RequestPart("username") String username,
            @RequestPart("email") String email,
            @RequestPart("password") String password,
            @RequestPart("role") String role,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        UserRegistrationDto registrationDto = new UserRegistrationDto(
            name,
            username,
            email,
            password,
            User.Role.valueOf(role.toUpperCase())
        );
        return ResponseEntity.ok(userService.registerWithImage(registrationDto, file));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody UserLoginDto loginDto) {
        return ResponseEntity.ok(userService.login(loginDto));
    }
}

// import com.farm.delivery.farmapi.dto.UserDTOs.AuthResponseDto;
// import com.farm.delivery.farmapi.dto.UserDTOs.UserLoginDto;
// import com.farm.delivery.farmapi.dto.UserDTOs.UserRegistrationDto;
// import com.farm.delivery.farmapi.dto.UserDTOs.UserResponseDto;
// import com.farm.delivery.farmapi.service.UserService;
// import jakarta.validation.Valid;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// @RestController
// @RequestMapping("/api/auth")
// @CrossOrigin(origins = "*")
// public class AuthController {

// private final UserService userService;

// public AuthController(UserService userService) {
// this.userService = userService;
// }

// @PostMapping("/register")
// public ResponseEntity<UserResponseDto> register(@Valid @RequestBody
// UserRegistrationDto registrationDto) {
// return ResponseEntity.ok(userService.register(registrationDto));
// }

// @PostMapping("/login")
// public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody UserLoginDto
// loginDto) {
// return ResponseEntity.ok(userService.login(loginDto));
// }
// }
