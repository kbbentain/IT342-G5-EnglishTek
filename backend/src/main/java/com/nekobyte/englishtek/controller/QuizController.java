package com.nekobyte.englishtek.controller;

import com.nekobyte.englishtek.dto.quiz.QuizRequest;
import com.nekobyte.englishtek.dto.quiz.QuizResponse;
import com.nekobyte.englishtek.dto.quiz.QuizSubmissionRequest;
import com.nekobyte.englishtek.dto.quiz.QuizSubmissionResponse;
import com.nekobyte.englishtek.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Quizzes", description = "Quiz management APIs")
public class QuizController {
    private final QuizService quizService;

    @Operation(summary = "Get quiz by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<QuizResponse> getQuiz(@PathVariable Long id) {
        return ResponseEntity.ok(quizService.getQuiz(id));
    }

    @Operation(summary = "Create new quiz")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QuizResponse> createQuiz(@Valid @RequestBody QuizRequest request) {
        return ResponseEntity.ok(quizService.createQuiz(request));
    }

    @Operation(summary = "Update quiz")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QuizResponse> updateQuiz(
            @PathVariable Long id,
            @Valid @RequestBody QuizRequest request
    ) {
        return ResponseEntity.ok(quizService.updateQuiz(id, request));
    }

    @Operation(summary = "Delete quiz")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long id) {
        quizService.deleteQuiz(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Start quiz")
    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<QuizResponse> startQuiz(@PathVariable Long id) {
        return ResponseEntity.ok(quizService.startQuiz(id));
    }

    @Operation(summary = "Submit quiz")
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<QuizSubmissionResponse> submitQuiz(
            @PathVariable Long id,
            @Valid @RequestBody QuizSubmissionRequest request
    ) {
        return ResponseEntity.ok(quizService.submitQuiz(id, request));
    }
}
