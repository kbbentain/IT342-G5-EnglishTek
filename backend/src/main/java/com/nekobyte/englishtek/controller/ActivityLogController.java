package com.nekobyte.englishtek.controller;

import com.nekobyte.englishtek.dto.activity.ActivityLogResponse;
import com.nekobyte.englishtek.service.ActivityLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/activity-log")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Activity Log", description = "Activity Log management APIs")
public class ActivityLogController {
    private final ActivityLogService activityLogService;

    @Operation(summary = "Get recent activity log (last 3 activities)")
    @GetMapping("/short")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<ActivityLogResponse>> getShortActivityLog() {
        return ResponseEntity.ok(activityLogService.getShortActivityLog());
    }

    @Operation(summary = "Get full activity log")
    @GetMapping("/long")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<ActivityLogResponse>> getLongActivityLog() {
        return ResponseEntity.ok(activityLogService.getLongActivityLog());
    }
}
