package com.nekobyte.englishtek.controller;

import com.nekobyte.englishtek.dto.chapter.ChapterRequest;
import com.nekobyte.englishtek.dto.chapter.ChapterResponse;
import com.nekobyte.englishtek.dto.chapter.ChapterDetailResponse;
import com.nekobyte.englishtek.dto.chapter.ChapterListResponse;
import com.nekobyte.englishtek.dto.chapter.ChapterRearrangeRequest;
import com.nekobyte.englishtek.service.ChapterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chapters")
@RequiredArgsConstructor
@Tag(name = "Chapters", description = "Chapter management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ChapterController {
    private final ChapterService chapterService;

    @Operation(summary = "Get all chapters")
    @GetMapping
    public ResponseEntity<List<ChapterListResponse>> getAllChapters() {
        return ResponseEntity.ok(chapterService.getAllChapters());
    }

    @Operation(summary = "Get chapter by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ChapterDetailResponse> getChapter(@PathVariable Long id) {
        return ResponseEntity.ok(chapterService.getChapter(id));
    }

    @Operation(summary = "Create new chapter")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ChapterResponse> createChapter(
            @ModelAttribute @Valid ChapterRequest request) {
        return ResponseEntity.ok(chapterService.createChapter(request));
    }

    @Operation(summary = "Update chapter")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ChapterResponse> updateChapter(
            @PathVariable Long id,
            @ModelAttribute @Valid ChapterRequest request) {
        return ResponseEntity.ok(chapterService.updateChapter(id, request));
    }

    @Operation(summary = "Rearrange chapter items (lessons and quizzes)")
    @PutMapping("/{id}/rearrange")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ChapterDetailResponse> rearrangeChapterItems(
            @PathVariable Long id,
            @RequestBody @Valid ChapterRearrangeRequest request) {
        return ResponseEntity.ok(chapterService.rearrangeChapterItems(id, request));
    }

    @Operation(summary = "Delete chapter")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteChapter(@PathVariable Long id) {
        chapterService.deleteChapter(id);
        return ResponseEntity.noContent().build();
    }
}
