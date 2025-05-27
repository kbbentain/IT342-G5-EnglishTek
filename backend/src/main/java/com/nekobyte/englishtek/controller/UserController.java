package com.nekobyte.englishtek.controller;

import com.nekobyte.englishtek.dto.auth.AuthResponse;
import com.nekobyte.englishtek.dto.user.UpdateUserByAdminFormRequest;
import com.nekobyte.englishtek.dto.user.UpdateUserRequest;
import com.nekobyte.englishtek.dto.user.UserResponse;
import com.nekobyte.englishtek.service.UserService;
import com.nekobyte.englishtek.validation.OnUpdate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "Endpoints for managing users")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = "Get current user details")
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUserDetails());
    }

    @Operation(summary = "Update current user")
    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AuthResponse> updateCurrentUser(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String existingPassword,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) Boolean passwordUpdateRequested,
            @RequestPart(required = false) MultipartFile avatar) {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername(username);
        request.setName(name);
        request.setEmail(email);
        request.setExistingPassword(existingPassword);
        request.setNewPassword(newPassword);
        request.setBio(bio);
        request.setPasswordUpdateRequested(passwordUpdateRequested != null ? passwordUpdateRequested : false);
        return ResponseEntity.ok(userService.updateCurrentUser(request, avatar));
    }

    @Operation(summary = "Delete current user")
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUser() {
        userService.deleteCurrentUser();
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all users")
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(summary = "Update user by ID")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthResponse> updateUser(
            @PathVariable Long id,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String existingPassword,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) Boolean passwordUpdateRequested,
            @RequestPart(required = false) MultipartFile avatar) {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername(username);
        request.setName(name);
        request.setEmail(email);
        request.setExistingPassword(existingPassword);
        request.setNewPassword(newPassword);
        request.setBio(bio);
        request.setPasswordUpdateRequested(passwordUpdateRequested != null ? passwordUpdateRequested : false);
        return ResponseEntity.ok(userService.updateUserById(id, request, avatar));
    }

    @Operation(summary = "Delete user by ID")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
