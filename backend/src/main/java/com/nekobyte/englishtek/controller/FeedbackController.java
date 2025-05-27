package com.nekobyte.englishtek.controller;

import com.nekobyte.englishtek.dto.feedback.FeedbackRequest;
import com.nekobyte.englishtek.dto.feedback.FeedbackResponse;
import com.nekobyte.englishtek.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/feedbacks")
@RequiredArgsConstructor
@Tag(name = "Feedbacks", description = "Feedback management APIs")
@SecurityRequirement(name = "bearerAuth")
public class FeedbackController {
    private final FeedbackService feedbackService;

    @Operation(summary = "Submit feedback for a chapter")
    @PostMapping("/submit")
    public ResponseEntity<FeedbackResponse> submitFeedback(@RequestBody @Valid FeedbackRequest request) {
        return ResponseEntity.ok(feedbackService.submitFeedback(request));
    }

    @Operation(summary = "Get feedback by ID")
    @GetMapping("/{id}")
    public ResponseEntity<FeedbackResponse> getFeedback(@PathVariable Long id) {
        return ResponseEntity.ok(feedbackService.getFeedback(id));
    }

    @Operation(summary = "Get all feedbacks for a chapter")
    @GetMapping("/get-feedbacks/{chapter_id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FeedbackResponse>> getFeedbacksByChapter(@PathVariable("chapter_id") Long chapterId) {
        return ResponseEntity.ok(feedbackService.getFeedbacksByChapter(chapterId));
    }

    @Operation(summary = "Update feedback")
    @PutMapping("/{id}")
    public ResponseEntity<FeedbackResponse> updateFeedback(
            @PathVariable Long id,
            @RequestBody @Valid FeedbackRequest request) {
        return ResponseEntity.ok(feedbackService.updateFeedback(id, request));
    }

    @Operation(summary = "Delete feedback")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable Long id) {
        feedbackService.deleteFeedback(id);
        return ResponseEntity.ok().build();
    }
}
