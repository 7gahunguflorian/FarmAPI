package com.farm.delivery.farmapi.controller;

import com.farm.delivery.farmapi.dto.UserDTOs.UserResponseDto;
import com.farm.delivery.farmapi.dto.UserDTOs.UpdateProfileImageDto;
import com.farm.delivery.farmapi.model.User;
import com.farm.delivery.farmapi.service.FileStorageService;
import com.farm.delivery.farmapi.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    private final FileStorageService fileStorageService;

    public UserController(UserService userService, FileStorageService fileStorageService) {
        this.userService = userService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.getCurrentUser().getId() == #id")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getUsersByRole(@PathVariable User.Role role) {
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser() {
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(new UserResponseDto(
            currentUser.getId(),
            currentUser.getName(),
            currentUser.getEmail(),
            currentUser.getUsername(),
            currentUser.getRole(),
            currentUser.getProfileImageUrl()
        ));
    }

    @PostMapping("/profile-image")
    public ResponseEntity<UpdateProfileImageDto> uploadProfileImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        String fileName = fileStorageService.storeFile(file);
        String imageUrl = "/images/" + fileName;
        UpdateProfileImageDto response = userService.updateProfileImage(userDetails.getUsername(), imageUrl);
        return ResponseEntity.ok(response);
    }
}
