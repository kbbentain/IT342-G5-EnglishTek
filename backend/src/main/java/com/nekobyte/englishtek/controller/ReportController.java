package com.nekobyte.englishtek.controller;

import com.nekobyte.englishtek.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/report")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Report generation APIs")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {
    private final ReportService reportService;

    @Operation(summary = "Generate current user's progress report")
    @GetMapping
    public ResponseEntity<byte[]> generateCurrentUserReport() throws IOException {
        byte[] report = reportService.generateUserReport(null);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("progress_report.pdf")
                .build());
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(report);
    }

    @Operation(summary = "Generate user progress report (Admin only)")
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> generateUserReport(@PathVariable Long userId) throws IOException {
        byte[] report = reportService.generateUserReport(userId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("user_" + userId + "_progress_report.pdf")
                .build());
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(report);
    }
}
