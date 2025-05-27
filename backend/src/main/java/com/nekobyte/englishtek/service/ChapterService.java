package com.nekobyte.englishtek.service;

import com.nekobyte.englishtek.dto.chapter.ChapterRequest;
import com.nekobyte.englishtek.dto.chapter.ChapterResponse;
import com.nekobyte.englishtek.dto.chapter.ChapterDetailResponse;
import com.nekobyte.englishtek.dto.chapter.ChapterListResponse;
import com.nekobyte.englishtek.dto.chapter.ChapterRearrangeRequest;
import com.nekobyte.englishtek.model.Chapter;
import com.nekobyte.englishtek.model.ChapterStatus;
import com.nekobyte.englishtek.model.User;
import com.nekobyte.englishtek.model.Quiz;
import com.nekobyte.englishtek.model.Lesson;
import com.nekobyte.englishtek.repository.ChapterRepository;
import com.nekobyte.englishtek.repository.LessonAttemptRepository;
import com.nekobyte.englishtek.repository.QuizAttemptRepository;
import com.nekobyte.englishtek.repository.UserRepository;
import com.nekobyte.englishtek.repository.FeedbackRepository;
import com.nekobyte.englishtek.repository.UserBadgeRepository;
import com.nekobyte.englishtek.repository.BadgeRepository;
import com.nekobyte.englishtek.service.BadgeService;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChapterService {
    private final ChapterRepository chapterRepository;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final LessonAttemptRepository lessonAttemptRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final FeedbackRepository feedbackRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final BadgeService badgeService;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalStateException("Current user not found"));
    }

    public List<ChapterListResponse> getAllChapters() {
        User currentUser = getCurrentUser();
        
        // Create a copy of the chapters list to avoid concurrent modification
        List<Chapter> chapters = new ArrayList<>(chapterRepository.findAll());
        chapters.sort(Comparator.comparing(Chapter::getId));

        // Create a new list to store responses
        List<ChapterListResponse> responses = new ArrayList<>();

        // Process each chapter sequentially
        for (Chapter chapter : new ArrayList<>(chapters)) {  // Create another copy for iteration
            int totalTasks = countTotalTasks(chapter);
            int completedTasks = countCompletedTasks(chapter, currentUser.getId());
            ChapterStatus status = determineChapterStatus(chapter, currentUser.getId(), chapters, completedTasks, totalTasks);
            
            double progressPercentage = totalTasks > 0 ? (completedTasks * 100.0) / totalTasks : 0;
            
            responses.add(ChapterListResponse.builder()
                    .id(chapter.getId())
                    .icon(chapter.getIcon() != null ? "/api/v1/files/" + chapter.getIcon() : null)
                    .title(chapter.getTitle())
                    .totalTasks(totalTasks)
                    .completedTasks(completedTasks)
                    .progressPercentage(progressPercentage)
                    .status(status)
                    .build());
        }

        return responses;
    }

    public ChapterDetailResponse getChapter(Long id) {
        try {
            User currentUser = getCurrentUser();
            Chapter chapter = chapterRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Chapter not found"));
                    
            // Get all chapters and sort them by ID
            List<Chapter> chapters = chapterRepository.findAll();
            chapters.sort(Comparator.comparing(Chapter::getId));
            
            // Calculate progress and status
            int totalTasks = countTotalTasks(chapter);
            int completedTasks = countCompletedTasks(chapter, currentUser.getId());
            
            System.out.println("About to determine status for chapter " + id);
            ChapterStatus status = determineChapterStatus(chapter, currentUser.getId(), chapters, completedTasks, totalTasks);
            System.out.println("Status determined: " + status);
            
            // Only check if chapter is locked for non-admin users
            if (!currentUser.getRole().name().equals("ADMIN") && status == ChapterStatus.LOCKED) {
                throw new IllegalStateException("This chapter is locked. Please complete the previous chapter first.");
            }
            
            try {
                System.out.println("About to map to detail response");
                ChapterDetailResponse response = mapToDetailResponse(chapter, currentUser, status, completedTasks, totalTasks);
                System.out.println("Response mapped successfully");
                return response;
            } catch (Exception e) {
                System.err.println("Error in mapToDetailResponse: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        } catch (Exception e) {
            System.err.println("Error in getChapter: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional
    public ChapterResponse createChapter(ChapterRequest request) {
        Chapter chapter = new Chapter();
        chapter.setTitle(request.getTitle());
        chapter.setDescription(request.getDescription());

        if (request.getIcon() != null && !request.getIcon().isEmpty()) {
            String fileName = fileStorageService.storeFile(request.getIcon());
            chapter.setIcon(fileName);
        }

        chapter = chapterRepository.save(chapter);
        return mapToResponse(chapter);
    }

    @Transactional
    public ChapterResponse updateChapter(Long id, ChapterRequest request) {
        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Chapter not found"));

        chapter.setTitle(request.getTitle());
        chapter.setDescription(request.getDescription());

        if (request.getIcon() != null && !request.getIcon().isEmpty()) {
            // Delete old icon if it exists
            if (chapter.getIcon() != null) {
                fileStorageService.deleteFile(chapter.getIcon());
            }
            String fileName = fileStorageService.storeFile(request.getIcon());
            chapter.setIcon(fileName);
        }

        chapter = chapterRepository.save(chapter);
        return mapToResponse(chapter);
    }

    @Transactional
    public void deleteChapter(Long id) {
        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Chapter not found"));
        
        // Delete badges from all quizzes in the chapter
        for (Quiz quiz : chapter.getQuizzes()) {
            if (quiz.getBadge() != null) {
                badgeService.deleteBadge(quiz.getBadge().getId());
            }
        }
        
        // Delete all feedbacks for this chapter
        feedbackRepository.deleteByChapterId(id);
        
        // Delete icon file if it exists
        if (chapter.getIcon() != null) {
            fileStorageService.deleteFile(chapter.getIcon());
        }
        
        chapterRepository.delete(chapter);
    }

    @Transactional
    public ChapterDetailResponse rearrangeChapterItems(Long chapterId, ChapterRearrangeRequest request) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new IllegalArgumentException("Chapter not found"));

        // Delete all lesson attempts for lessons in this chapter
        for (Lesson lesson : chapter.getLessons()) {
            lessonAttemptRepository.deleteByLessonId(lesson.getId());
        }

        // Delete all quiz attempts and related user badges for quizzes in this chapter
        for (Quiz quiz : chapter.getQuizzes()) {
            quizAttemptRepository.deleteByQuizId(quiz.getId());
            // Delete user badges where the badge is from this quiz
            if (quiz.getBadge() != null) {
                userBadgeRepository.deleteByBadgeId(quiz.getBadge().getId());
            }
        }

        // Create maps to store items by their IDs for quick access
        Map<Long, Lesson> lessonMap = chapter.getLessons().stream()
                .collect(Collectors.toMap(Lesson::getId, lesson -> lesson));
        Map<Long, Quiz> quizMap = chapter.getQuizzes().stream()
                .collect(Collectors.toMap(Quiz::getId, quiz -> quiz));

        // Process each item in the new order and update their order field
        for (int i = 0; i < request.getOrder().size(); i++) {
            ChapterRearrangeRequest.ChapterItemOrder item = request.getOrder().get(i);
            if ("lesson".equalsIgnoreCase(item.getType())) {
                Lesson lesson = lessonMap.get(item.getId());
                if (lesson == null) {
                    throw new IllegalArgumentException("Lesson with ID " + item.getId() + " not found in this chapter");
                }
                lesson.setOrder(i);
            } else if ("quiz".equalsIgnoreCase(item.getType())) {
                Quiz quiz = quizMap.get(item.getId());
                if (quiz == null) {
                    throw new IllegalArgumentException("Quiz with ID " + item.getId() + " not found in this chapter");
                }
                quiz.setOrder(i);
            } else {
                throw new IllegalArgumentException("Invalid item type: " + item.getType());
            }
        }

        // Save the updated chapter
        chapterRepository.save(chapter);

        // Return the updated chapter details
        User currentUser = getCurrentUser();
        int totalTasks = countTotalTasks(chapter);
        int completedTasks = countCompletedTasks(chapter, currentUser.getId());
        List<Chapter> chapters = chapterRepository.findAll();
        chapters.sort(Comparator.comparing(Chapter::getId));
        ChapterStatus status = determineChapterStatus(chapter, currentUser.getId(), chapters, completedTasks, totalTasks);

        return mapToDetailResponse(chapter, currentUser, status, completedTasks, totalTasks);
    }

    private ChapterResponse mapToResponse(Chapter chapter) {
        String iconUrl = chapter.getIcon() != null
                ? "/api/v1/files/" + chapter.getIcon()
                : null;

        List<ChapterResponse.ChapterItemDto> items = new ArrayList<>();
        Long userId = getCurrentUser().getId();

        // Check if user has completed feedback for this chapter
        boolean hasCompletedFeedback = feedbackRepository
            .findByUserIdAndChapterId(userId, chapter.getId())
            .isPresent();

        // Create a list of ChapterItems that combines both lessons and quizzes
        List<ChapterItem> chapterItems = new ArrayList<>();
        
        if (chapter.getLessons() != null) {
            int order = 0;
            for (Lesson lesson : chapter.getLessons()) {
                boolean completed = lessonAttemptRepository
                    .findByUserIdAndLessonId(userId, lesson.getId())
                    .map(attempt -> attempt.getCompletedAt() != null)
                    .orElse(false);
                chapterItems.add(new ChapterItem("lesson", lesson.getId(), lesson.getTitle(), lesson.getCreatedAt(), completed, order++));
            }
        }
        
        if (chapter.getQuizzes() != null) {
            int order = chapter.getLessons() != null ? chapter.getLessons().size() : 0;
            for (Quiz quiz : chapter.getQuizzes()) {
                boolean completed = quizAttemptRepository
                    .findByUserIdAndQuizId(userId, quiz.getId())
                    .map(attempt -> {
                        if (attempt.getCompletedAt() == null) return false;
                        // Quiz is only considered completed if score is at least 80% of max score
                        return attempt.getScore() >= (quiz.getMaxScore() * 0.8);
                    })
                    .orElse(false);
                chapterItems.add(new ChapterItem("quiz", quiz.getId(), quiz.getTitle(), quiz.getCreatedAt(), completed, order++));
            }
        }

        // Sort all items by creation time
        chapterItems.stream()
                .sorted(Comparator.comparing(ChapterItem::getCreatedAt))
                .forEach(item -> {
                    items.add(ChapterResponse.ChapterItemDto.builder()
                            .id(item.getId())
                            .type(item.getType())
                            .title(item.getTitle())
                            .order(items.size() + 1)
                            .completed(item.isCompleted())
                            .build());
                });

        return ChapterResponse.builder()
                .id(chapter.getId())
                .title(chapter.getTitle())
                .description(chapter.getDescription())
                .iconUrl(iconUrl)
                .items(items)
                .hasCompletedFeedback(hasCompletedFeedback)
                .build();
    }

    private ChapterListResponse mapToListResponse(Chapter chapter, Long userId, List<Chapter> allChapters) {
        // Create a list of ChapterItems that combines both lessons and quizzes
        List<ChapterItem> chapterItems = new ArrayList<>();
        
        if (chapter.getLessons() != null) {
            int order = 0;
            for (Lesson lesson : chapter.getLessons()) {
                boolean completed = lessonAttemptRepository
                    .findByUserIdAndLessonId(userId, lesson.getId())
                    .map(attempt -> attempt.getCompletedAt() != null)
                    .orElse(false);
                chapterItems.add(new ChapterItem("lesson", lesson.getId(), lesson.getTitle(), lesson.getCreatedAt(), completed, order++));
            }
        }
        
        if (chapter.getQuizzes() != null) {
            int order = chapter.getLessons() != null ? chapter.getLessons().size() : 0;
            for (Quiz quiz : chapter.getQuizzes()) {
                boolean completed = quizAttemptRepository
                    .findByUserIdAndQuizId(userId, quiz.getId())
                    .map(attempt -> {
                        if (attempt.getCompletedAt() == null) return false;
                        return attempt.getScore() >= (quiz.getMaxScore() * 0.8);
                    })
                    .orElse(false);
                chapterItems.add(new ChapterItem("quiz", quiz.getId(), quiz.getTitle(), quiz.getCreatedAt(), completed, order++));
            }
        }

        // Sort all items by creation time
        List<ChapterDetailResponse.ChapterItemDto> items = chapterItems.stream()
                .sorted(Comparator.comparing(ChapterItem::getOrder))
                .map(item -> ChapterDetailResponse.ChapterItemDto.builder()
                        .id(item.getId())
                        .type(item.getType())
                        .title(item.getTitle())
                        .order(item.getOrder() + 1)
                        .completed(item.isCompleted())
                        .build())
                .collect(Collectors.toList());

        int totalTasks = countTotalTasks(chapter);
        int completedTasks = countCompletedTasks(chapter, userId);
        double progressPercentage = totalTasks > 0 ? (completedTasks * 100.0) / totalTasks : 0;

        ChapterDetailResponse response = ChapterDetailResponse.builder()
                .id(chapter.getId())
                .title(chapter.getTitle())
                .description(chapter.getDescription())
                .iconUrl(chapter.getIcon() != null ? "/api/v1/files/" + chapter.getIcon() : null)
                .items(items)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .progressPercentage(progressPercentage)
                .build();

        response.setCompletedTasks(completedTasks);
        response.setProgressPercentage(progressPercentage);
        
        // Set chapter status
        response.setStatus(determineChapterStatus(chapter, userId, allChapters, completedTasks, totalTasks));

        return ChapterListResponse.builder()
                .id(chapter.getId())
                .title(chapter.getTitle())
                .icon(chapter.getIcon() != null ? "/api/v1/files/" + chapter.getIcon() : null)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .progressPercentage(progressPercentage)
                .status(response.getStatus())
                .build();
    }

    private ChapterDetailResponse mapToDetailResponse(Chapter chapter, User currentUser, ChapterStatus status, int completedTasks, int totalTasks) {
        try {
            System.out.println("Starting mapToDetailResponse");
            System.out.println("Chapter: " + chapter.getId() + ", User: " + currentUser.getId() + ", Status: " + status);
            
            List<ChapterDetailResponse.ChapterItemDto> items = new ArrayList<>();
            
            // Check if user has completed feedback for this chapter
            boolean hasCompletedFeedback = feedbackRepository
                .findByUserIdAndChapterId(currentUser.getId(), chapter.getId())
                .isPresent();
            
            System.out.println("hasCompletedFeedback: " + hasCompletedFeedback);
            
            // Create a list of ChapterItems that combines both lessons and quizzes
            List<ChapterItem> chapterItems = new ArrayList<>();
            
            if (chapter.getLessons() != null) {
                System.out.println("Processing lessons: " + chapter.getLessons().size());
                for (Lesson lesson : chapter.getLessons()) {
                    try {
                        boolean completed = lessonAttemptRepository
                            .findByUserIdAndLessonId(currentUser.getId(), lesson.getId())
                            .map(attempt -> attempt.getCompletedAt() != null)
                            .orElse(false);
                        chapterItems.add(new ChapterItem("lesson", lesson.getId(), lesson.getTitle(), lesson.getCreatedAt(), completed, lesson.getOrder()));
                        System.out.println("Added lesson: " + lesson.getId() + ", completed: " + completed);
                    } catch (Exception e) {
                        System.err.println("Error processing lesson " + lesson.getId() + ": " + e.getMessage());
                        throw e;
                    }
                }
            }
            
            if (chapter.getQuizzes() != null) {
                System.out.println("Processing quizzes: " + chapter.getQuizzes().size());
                for (Quiz quiz : chapter.getQuizzes()) {
                    try {
                        boolean completed = quizAttemptRepository
                            .findByUserIdAndQuizId(currentUser.getId(), quiz.getId())
                            .map(attempt -> {
                                if (attempt.getCompletedAt() == null) return false;
                                // Quiz is only considered completed if score is at least 80% of max score
                                return attempt.getScore() >= (quiz.getMaxScore() * 0.8);
                            })
                            .orElse(false);
                        chapterItems.add(new ChapterItem("quiz", quiz.getId(), quiz.getTitle(), quiz.getCreatedAt(), completed, quiz.getOrder()));
                        System.out.println("Added quiz: " + quiz.getId() + ", completed: " + completed);
                    } catch (Exception e) {
                        System.err.println("Error processing quiz " + quiz.getId() + ": " + e.getMessage());
                        throw e;
                    }
                }
            }

            System.out.println("Sorting items: " + chapterItems.size());
            // Sort items by order
            chapterItems.stream()
                    .sorted(Comparator.comparing(ChapterItem::getOrder))
                    .forEach(item -> {
                        try {
                            items.add(ChapterDetailResponse.ChapterItemDto.builder()
                                    .id(item.getId())
                                    .type(item.getType())
                                    .title(item.getTitle())
                                    .order(item.getOrder() + 1)
                                    .completed(item.isCompleted())
                                    .build());
                            System.out.println("Added sorted item: " + item.getId());
                        } catch (Exception e) {
                            System.err.println("Error adding sorted item " + item.getId() + ": " + e.getMessage());
                            throw e;
                        }
                    });

            double progressPercentage = totalTasks > 0 ? (completedTasks * 100.0) / totalTasks : 0;
            System.out.println("Progress: " + completedTasks + "/" + totalTasks + " = " + progressPercentage + "%");

            System.out.println("Building ChapterDetailResponse");
            return ChapterDetailResponse.builder()
                    .id(chapter.getId())
                    .title(chapter.getTitle())
                    .description(chapter.getDescription())
                    .iconUrl(chapter.getIcon() != null ? "/api/v1/files/" + chapter.getIcon() : null)
                    .items(items)
                    .hasCompletedFeedback(hasCompletedFeedback)
                    .status(status)
                    .totalTasks(totalTasks)
                    .completedTasks(completedTasks)
                    .progressPercentage(progressPercentage)
                    .build();
        } catch (Exception e) {
            System.err.println("Error in mapToDetailResponse: " + e.getMessage());
            throw e;
        }
    }

    private ChapterDetailResponse mapToDetailResponse(Chapter chapter, User currentUser, ChapterStatus status, 
            int completedTasks, int totalTasks, Map<String, Integer> itemOrderMap) {
        return mapToDetailResponse(chapter, currentUser, status, completedTasks, totalTasks);
    }

    private ChapterStatus determineChapterStatus(Chapter chapter, Long userId, List<Chapter> allChapters, int completedTasks, int totalTasks) {
        // Create a copy of the chapters list to avoid concurrent modification
        List<Chapter> chapters = new ArrayList<>(allChapters);
        
        System.out.println("\n=== Determining status for Chapter " + chapter.getId() + " ===");
        System.out.println("User: " + userId + ", Role: " + getCurrentUser().getRole());
        System.out.println("Progress: " + completedTasks + "/" + totalTasks + " tasks completed");
        
        // Get the index of the current chapter in the sequence
        int chapterIndex = chapters.indexOf(chapter);
        System.out.println("Chapter index in sequence: " + chapterIndex);
        
        // For non-first chapters, check if they're locked
        if (chapterIndex > 0) {
            // Get the previous chapter
            Chapter previousChapter = chapters.get(chapterIndex - 1);
            
            // Count completed tasks for previous chapter
            int prevCompletedTasks = countCompletedTasks(previousChapter, userId);
            int prevTotalTasks = countTotalTasks(previousChapter);
            
            System.out.println("Previous chapter " + previousChapter.getId() + " progress: " + prevCompletedTasks + "/" + prevTotalTasks);
            
            // Previous chapter must be completed to unlock this one
            boolean previousChapterCompleted = prevCompletedTasks >= prevTotalTasks;
            System.out.println("Previous chapter completed? " + previousChapterCompleted);
            
            if (!previousChapterCompleted) {
                System.out.println("Previous chapter not completed - returning LOCKED");
                return ChapterStatus.LOCKED;
            }
        }
        
        // At this point, the chapter is either the first one or unlocked
        // Now determine if it's in progress or completed
        if (completedTasks >= totalTasks) {
            System.out.println("All tasks completed - returning COMPLETED");
            return ChapterStatus.COMPLETED;
        } else if (completedTasks > 0) {
            System.out.println("Some tasks completed - returning IN_PROGRESS");
            return ChapterStatus.IN_PROGRESS;
        } else {
            System.out.println("No tasks completed - returning AVAILABLE");
            return ChapterStatus.AVAILABLE;
        }
    }
    
    private int countTotalTasks(Chapter chapter) {
        int totalTasks = 0;
        
        if (chapter.getLessons() != null) {
            totalTasks += chapter.getLessons().size();
        }
        
        if (chapter.getQuizzes() != null) {
            totalTasks += chapter.getQuizzes().size();
        }
        
        System.out.println("Chapter " + chapter.getId() + " total tasks: " + totalTasks + 
            " (lessons: " + (chapter.getLessons() != null ? chapter.getLessons().size() : 0) + 
            ", quizzes: " + (chapter.getQuizzes() != null ? chapter.getQuizzes().size() : 0) + ")");
            
        return totalTasks;
    }
    
    private int countCompletedTasks(Chapter chapter, Long userId) {
        int completedLessons = 0;
        int completedQuizzes = 0;
        
        System.out.println("\nCounting completed tasks for Chapter " + chapter.getId() + ":");
        
        if (chapter.getLessons() != null) {
            for (var lesson : chapter.getLessons()) {
                var attempt = lessonAttemptRepository.findByUserIdAndLessonId(userId, lesson.getId());
                boolean completed = attempt.map(a -> a.getCompletedAt() != null).orElse(false);
                System.out.println("  Lesson " + lesson.getId() + ": " + 
                    (attempt.isPresent() ? 
                        ("attempt found, completed: " + completed + ", completedAt: " + attempt.get().getCompletedAt()) 
                        : "no attempt found"));
                if (completed) completedLessons++;
            }
        }

        if (chapter.getQuizzes() != null) {
            for (var quiz : chapter.getQuizzes()) {
                var attempt = quizAttemptRepository.findByUserIdAndQuizId(userId, quiz.getId());
                String status = attempt.map(a -> {
                    if (a.getCompletedAt() == null) return "incomplete";
                    double score = a.getScore();
                    double maxScore = quiz.getMaxScore();
                    boolean passed = score >= (maxScore * 0.8);
                    return String.format("score: %.1f/%.1f (%.1f%%) - %s", 
                        score, maxScore, (score/maxScore)*100, 
                        passed ? "PASSED" : "FAILED");
                }).orElse("no attempt found");
                
                System.out.println("  Quiz " + quiz.getId() + ": " + status);
                
                if (attempt.map(a -> {
                    if (a.getCompletedAt() == null) return false;
                    return a.getScore() >= (quiz.getMaxScore() * 0.8);
                }).orElse(false)) {
                    completedQuizzes++;
                }
            }
        }
        
        System.out.println("Chapter " + chapter.getId() + " completed tasks: " + (completedLessons + completedQuizzes) + 
            " (lessons: " + completedLessons + ", quizzes: " + completedQuizzes + ")");
            
        return completedLessons + completedQuizzes;
    }

    // Helper class to combine lessons and quizzes for sorting
    @Data
    @AllArgsConstructor
    private static class ChapterItem {
        private String type;
        private Long id;
        private String title;
        private LocalDateTime createdAt;
        private boolean completed;
        private int order;
    }
}
