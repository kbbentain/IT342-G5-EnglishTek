package com.nekobyte.englishtek.controller;

import com.nekobyte.englishtek.dto.badge.BadgeRequest;
import com.nekobyte.englishtek.dto.badge.BadgeResponse;
import com.nekobyte.englishtek.dto.badge.UserBadgeResponse;
import com.nekobyte.englishtek.service.BadgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/badges")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Badges", description = "Badge management APIs")
public class BadgeController {
    private final BadgeService badgeService;

    @Operation(summary = "Get all badges")
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<BadgeResponse>> getAllBadges() {
        return ResponseEntity.ok(badgeService.getAllBadges());
    }

    @Operation(summary = "Get badge by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<BadgeResponse> getBadge(@PathVariable Long id) {
        return ResponseEntity.ok(badgeService.getBadge(id));
    }

    @Operation(summary = "Get current user's badges")
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<UserBadgeResponse>> getMyBadges() {
        return ResponseEntity.ok(badgeService.getMyBadges());
    }

    @Operation(summary = "Create new badge")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BadgeResponse> createBadge(
            @RequestPart("name") String name,
            @RequestPart("description") String description,
            @RequestPart("icon") MultipartFile icon
    ) {
        return ResponseEntity.ok(badgeService.createBadge(name, description, icon));
    }

    @Operation(summary = "Update badge")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BadgeResponse> updateBadge(
            @PathVariable Long id,
            @RequestPart(value = "name", required = false) String name,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart(value = "icon", required = false) MultipartFile icon
    ) {
        return ResponseEntity.ok(badgeService.updateBadge(id, name, description, icon));
    }

    @Operation(summary = "Delete badge")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBadge(@PathVariable Long id) {
        badgeService.deleteBadge(id);
        return ResponseEntity.ok().build();
    }
}
