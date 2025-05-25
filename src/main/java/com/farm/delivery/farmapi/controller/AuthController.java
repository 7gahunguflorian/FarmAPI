package com.farm.delivery.farmapi.controller;

import com.farm.delivery.farmapi.dto.user.*;
import com.farm.delivery.farmapi.exception.NonAdminAccessException;
import com.farm.delivery.farmapi.model.User;
import com.farm.delivery.farmapi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
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

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRegistrationDto registrationDto) {
        return ResponseEntity.ok(userService.register(registrationDto));
    }

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
                User.Role.valueOf(role.toUpperCase()));
        return ResponseEntity.ok(userService.registerWithImage(registrationDto, file));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginDto loginDto) {
        try {
            AuthResponseDto response = userService.login(loginDto);
            return ResponseEntity.ok(response);
        } catch (NonAdminAccessException e) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(e.getMessage()));
        } catch (BadCredentialsException e) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Invalid username or password"));
        }
    }

    private record ErrorResponse(String message) {}
}
