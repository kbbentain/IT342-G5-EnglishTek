package com.nekobyte.englishtek.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Health check endpoint")
public class HealthController {

    @Operation(summary = "Check if the application is running")
    @GetMapping
    public ResponseEntity<Void> healthCheck() {
        return ResponseEntity.ok().build();
    }
}
