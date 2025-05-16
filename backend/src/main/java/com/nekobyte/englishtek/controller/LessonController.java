package com.nekobyte.englishtek.controller;

import com.nekobyte.englishtek.dto.lesson.LessonRequest;
import com.nekobyte.englishtek.dto.lesson.LessonResponse;
import com.nekobyte.englishtek.dto.lesson.LessonPdfResponse;
import com.nekobyte.englishtek.service.LessonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/lessons")
@RequiredArgsConstructor
@Tag(name = "Lesson", description = "Lesson management APIs")
@SecurityRequirement(name = "bearerAuth")
public class LessonController {
    private final LessonService lessonService;

    @GetMapping
    @Operation(summary = "Get all lessons")
    public ResponseEntity<List<LessonResponse>> getAllLessons() {
        return ResponseEntity.ok(lessonService.getAllLessons());
    }

    @GetMapping("/chapter/{chapterId}")
    @Operation(summary = "Get lessons by chapter")
    public ResponseEntity<List<LessonResponse>> getLessonsByChapter(@PathVariable Long chapterId) {
        return ResponseEntity.ok(lessonService.getLessonsByChapter(chapterId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get lesson by id")
    public ResponseEntity<LessonResponse> getLesson(@PathVariable Long id) {
        return ResponseEntity.ok(lessonService.getLesson(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new lesson")
    public ResponseEntity<LessonResponse> createLesson(@Valid @RequestBody LessonRequest request) {
        return ResponseEntity.ok(lessonService.createLesson(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing lesson")
    public ResponseEntity<LessonResponse> updateLesson(@PathVariable Long id, @Valid @RequestBody LessonRequest request) {
        return ResponseEntity.ok(lessonService.updateLesson(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a lesson")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long id) {
        lessonService.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "Start a lesson")
    public ResponseEntity<LessonResponse> startLesson(@PathVariable Long id) {
        return ResponseEntity.ok(lessonService.startLesson(id));
    }

    @PostMapping("/{id}/finish")
    @Operation(summary = "Finish a lesson")
    public ResponseEntity<Void> finishLesson(@PathVariable Long id) {
        lessonService.finishLesson(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Download lesson as PDF")
    public ResponseEntity<byte[]> getLessonPdf(@PathVariable Long id) throws IOException {
        LessonPdfResponse pdfResponse = lessonService.getLessonAsPdf(id);
        
        String sanitizedFileName = pdfResponse.getFileName().replaceAll("[^a-zA-Z0-9-_\\.]", "_");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename(sanitizedFileName)
                .build());
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfResponse.getContent());
    }
}
