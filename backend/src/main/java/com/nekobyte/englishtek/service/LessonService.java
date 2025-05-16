package com.nekobyte.englishtek.service;

import com.nekobyte.englishtek.dto.lesson.LessonPdfResponse;
import com.nekobyte.englishtek.dto.lesson.LessonRequest;
import com.nekobyte.englishtek.dto.lesson.LessonResponse;
import com.nekobyte.englishtek.model.*;
import com.nekobyte.englishtek.repository.ChapterRepository;
import com.nekobyte.englishtek.repository.LessonAttemptRepository;
import com.nekobyte.englishtek.repository.LessonRepository;
import com.nekobyte.englishtek.util.LessonPdfConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonService {
    private final LessonRepository lessonRepository;
    private final ChapterRepository chapterRepository;
    private final LessonAttemptRepository lessonAttemptRepository;
    private final UserService userService;
    private final LessonPdfConverter lessonPdfConverter;

    public List<LessonResponse> getAllLessons() {
        User currentUser = userService.getCurrentUser();
        return lessonRepository.findAll().stream()
                .map(lesson -> mapToResponse(lesson, currentUser))
                .collect(Collectors.toList());
    }

    public List<LessonResponse> getLessonsByChapter(Long chapterId) {
        User currentUser = userService.getCurrentUser();
        return lessonRepository.findByChapterId(chapterId).stream()
                .map(lesson -> mapToResponse(lesson, currentUser))
                .collect(Collectors.toList());
    }

    public LessonResponse getLesson(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));
        User currentUser = userService.getCurrentUser();
        return mapToResponse(lesson, currentUser);
    }

    @Transactional
    public LessonResponse createLesson(LessonRequest request) {
        Chapter chapter = chapterRepository.findById(request.getChapterId())
                .orElseThrow(() -> new IllegalArgumentException("Chapter not found"));

        Lesson lesson = new Lesson();
        lesson.setChapter(chapter);
        lesson.setTitle(request.getTitle());
        lesson.setDescription(request.getDescription());
        lesson.setContent(request.getContent());

        lesson = lessonRepository.save(lesson);
        return mapToResponse(lesson, userService.getCurrentUser());
    }

    @Transactional
    public LessonResponse updateLesson(Long id, LessonRequest request) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));

        Chapter chapter = chapterRepository.findById(request.getChapterId())
                .orElseThrow(() -> new IllegalArgumentException("Chapter not found"));

        lesson.setChapter(chapter);
        lesson.setTitle(request.getTitle());
        lesson.setDescription(request.getDescription());
        lesson.setContent(request.getContent());

        lesson = lessonRepository.save(lesson);
        return mapToResponse(lesson, userService.getCurrentUser());
    }

    @Transactional
    public void deleteLesson(Long id) {
        if (!lessonRepository.existsById(id)) {
            throw new IllegalArgumentException("Lesson not found");
        }
        lessonRepository.deleteById(id);
    }

    @Transactional
    public LessonResponse startLesson(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));
        User currentUser = userService.getCurrentUser();

        Optional<LessonAttempt> existingAttempt = lessonAttemptRepository
                .findByUserIdAndLessonId(currentUser.getId(), id);

        if (existingAttempt.isEmpty()) {
            LessonAttempt attempt = new LessonAttempt();
            attempt.setLesson(lesson);
            attempt.setUser(currentUser);
            attempt.setStartedAt(LocalDateTime.now());
            lessonAttemptRepository.save(attempt);
        }

        return mapToResponse(lesson, currentUser);
    }

    @Transactional
    public void finishLesson(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));
        User currentUser = userService.getCurrentUser();

        LessonAttempt attempt = lessonAttemptRepository
                .findByUserIdAndLessonId(currentUser.getId(), id)
                .orElseThrow(() -> new IllegalStateException("Cannot finish a lesson that hasn't been started. Please start the lesson first."));

        if (attempt.getStartedAt() == null) {
            throw new IllegalStateException("Cannot finish a lesson that hasn't been started. Please start the lesson first.");
        }

        attempt.setCompletedAt(LocalDateTime.now());
        lessonAttemptRepository.save(attempt);
    }

    @Transactional
    public LessonResponse completeLesson(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));
        User currentUser = userService.getCurrentUser();

        // Admin can complete lessons multiple times
        if (!currentUser.getRole().name().equals("ADMIN")) {
            LessonAttempt existingAttempt = lessonAttemptRepository
                    .findByUserIdAndLessonId(currentUser.getId(), id)
                    .filter(attempt -> attempt.getCompletedAt() != null)
                    .orElse(null);

            if (existingAttempt != null) {
                throw new IllegalStateException("This lesson has already been completed.");
            }
        }

        LessonAttempt attempt = lessonAttemptRepository
                .findByUserIdAndLessonId(currentUser.getId(), id)
                .orElseGet(() -> {
                    LessonAttempt newAttempt = new LessonAttempt();
                    newAttempt.setUser(currentUser);
                    newAttempt.setLesson(lesson);
                    return newAttempt;
                });

        attempt.setCompletedAt(LocalDateTime.now());
        lessonAttemptRepository.save(attempt);

        return mapToResponse(lesson, currentUser);
    }

    public LessonPdfResponse getLessonAsPdf(Long id) throws IOException {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));
        
        byte[] pdfContent = lessonPdfConverter.convertLessonToPdf(lesson);
        
        return LessonPdfResponse.builder()
                .content(pdfContent)
                .fileName(lesson.getTitle() + ".pdf")
                .build();
    }

    private LessonResponse mapToResponse(Lesson lesson, User currentUser) {
        boolean completed = lessonAttemptRepository
                .findByUserIdAndLessonId(currentUser.getId(), lesson.getId())
                .map(attempt -> attempt.getCompletedAt() != null)
                .orElse(false);

        return LessonResponse.builder()
                .id(lesson.getId())
                .chapterId(lesson.getChapter().getId())
                .title(lesson.getTitle())
                .description(lesson.getDescription())
                .content(lesson.getContent())
                .completed(completed)
                .build();
    }
}
