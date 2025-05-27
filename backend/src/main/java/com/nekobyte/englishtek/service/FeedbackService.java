package com.nekobyte.englishtek.service;

import com.nekobyte.englishtek.dto.feedback.FeedbackRequest;
import com.nekobyte.englishtek.dto.feedback.FeedbackResponse;
import com.nekobyte.englishtek.model.Chapter;
import com.nekobyte.englishtek.model.Feedback;
import com.nekobyte.englishtek.model.User;
import com.nekobyte.englishtek.repository.ChapterRepository;
import com.nekobyte.englishtek.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final ChapterRepository chapterRepository;
    private final UserService userService;

    @Transactional
    public FeedbackResponse submitFeedback(FeedbackRequest request) {
        User currentUser = userService.getCurrentUser();
        Chapter chapter = chapterRepository.findById(request.getChapterId())
                .orElseThrow(() -> new IllegalArgumentException("Chapter not found"));

        // Check if user has already submitted feedback for this chapter
        feedbackRepository.findByUserIdAndChapterId(currentUser.getId(), chapter.getId())
                .ifPresent(existingFeedback -> {
                    throw new IllegalStateException("You have already submitted feedback for this chapter");
                });

        Feedback feedback = new Feedback();
        feedback.setUser(currentUser);
        feedback.setChapter(chapter);
        feedback.setRating(request.getRating());
        feedback.setFeedbackText(request.getFeedbackText());
        feedback.setFeedbackKeyword(request.getFeedbackKeyword());

        feedback = feedbackRepository.save(feedback);
        return mapToResponse(feedback);
    }

    @Transactional
    public FeedbackResponse updateFeedback(Long id, FeedbackRequest request) {
        User currentUser = userService.getCurrentUser();
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found"));

        // Only allow users to update their own feedback
        if (!feedback.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("You can only update your own feedback");
        }

        // If chapter ID is being changed, verify the new chapter exists
        if (!feedback.getChapter().getId().equals(request.getChapterId())) {
            Chapter newChapter = chapterRepository.findById(request.getChapterId())
                    .orElseThrow(() -> new IllegalArgumentException("Chapter not found"));
            feedback.setChapter(newChapter);
        }

        feedback.setRating(request.getRating());
        feedback.setFeedbackText(request.getFeedbackText());
        feedback.setFeedbackKeyword(request.getFeedbackKeyword());

        feedback = feedbackRepository.save(feedback);
        return mapToResponse(feedback);
    }

    public FeedbackResponse getFeedback(Long id) {
        User currentUser = userService.getCurrentUser();
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found"));

        // Only allow users to view their own feedback unless they're an admin
        if (!feedback.getUser().getId().equals(currentUser.getId()) && !currentUser.getRole().equals("ROLE_ADMIN")) {
            throw new IllegalStateException("You can only view your own feedback");
        }

        return mapToResponse(feedback);
    }

    public List<FeedbackResponse> getFeedbacksByChapter(Long chapterId) {
        // Verify chapter exists
        if (!chapterRepository.existsById(chapterId)) {
            throw new IllegalArgumentException("Chapter not found");
        }

        return feedbackRepository.findByChapterId(chapterId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteFeedback(Long id) {
        User currentUser = userService.getCurrentUser();
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found"));

        // Only allow users to delete their own feedback unless they're an admin
        if (!feedback.getUser().getId().equals(currentUser.getId()) && !currentUser.getRole().equals("ROLE_ADMIN")) {
            throw new IllegalStateException("You can only delete your own feedback");
        }

        feedbackRepository.delete(feedback);
    }

    private FeedbackResponse mapToResponse(Feedback feedback) {
        return FeedbackResponse.builder()
                .id(feedback.getId())
                .userId(feedback.getUser().getId())
                .username(feedback.getUser().getUsername())
                .chapterId(feedback.getChapter().getId())
                .chapterTitle(feedback.getChapter().getTitle())
                .rating(feedback.getRating())
                .feedbackText(feedback.getFeedbackText())
                .feedbackKeyword(feedback.getFeedbackKeyword())
                .createdAt(feedback.getCreatedAt())
                .updatedAt(feedback.getUpdatedAt())
                .build();
    }
}
