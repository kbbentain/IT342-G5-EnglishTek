package com.nekobyte.englishtek.controller;

import com.nekobyte.englishtek.dto.dashboard.AdminDashboardResponse;
import com.nekobyte.englishtek.dto.admin.AdminChangePasswordRequest;
import com.nekobyte.englishtek.service.AdminDashboardService;
import com.nekobyte.englishtek.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin", description = "Admin management APIs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {
    private final AdminDashboardService adminDashboardService;
    private final UserService userService;

    @Operation(summary = "Get admin dashboard statistics")
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboardStats() {
        return ResponseEntity.ok(adminDashboardService.getDashboardStats());
    }

    @Operation(summary = "Change user's password")
    @PutMapping("/change-password/{id}")
    public ResponseEntity<Void> changeUserPassword(
            @PathVariable Long id,
            @RequestBody @Valid AdminChangePasswordRequest request) {
        userService.changeUserPassword(id, request.getNewPassword());
        return ResponseEntity.ok().build();
    }
}
