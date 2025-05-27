package com.nekobyte.englishtek.service;

import com.nekobyte.englishtek.dto.activity.ActivityLogResponse;
import com.nekobyte.englishtek.model.User;
import com.nekobyte.englishtek.repository.LessonAttemptRepository;
import com.nekobyte.englishtek.repository.QuizAttemptRepository;
import com.nekobyte.englishtek.repository.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityLogService {
    private final LessonAttemptRepository lessonAttemptRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserService userService;

    public List<ActivityLogResponse> getShortActivityLog() {
        return getAllActivities().stream()
                .limit(3)
                .collect(Collectors.toList());
    }

    public List<ActivityLogResponse> getLongActivityLog() {
        return getAllActivities();
    }

    private List<ActivityLogResponse> getAllActivities() {
        User currentUser = userService.getCurrentUser();
        List<ActivityLogResponse> activities = new ArrayList<>();

        // Get lesson attempts
        lessonAttemptRepository.findByUserId(currentUser.getId()).stream()
                .filter(attempt -> attempt.getCompletedAt() != null)
                .forEach(attempt -> activities.add(
                        ActivityLogResponse.builder()
                                .type("lesson")
                                .name(attempt.getLesson().getTitle())
                                .date(attempt.getCompletedAt())
                                .build()
                ));

        // Get quiz attempts
        quizAttemptRepository.findByUserId(currentUser.getId()).stream()
                .filter(attempt -> attempt.getCompletedAt() != null)
                .forEach(attempt -> activities.add(
                        ActivityLogResponse.builder()
                                .type("quiz")
                                .name(attempt.getQuiz().getTitle())
                                .date(attempt.getCompletedAt())
                                .build()
                ));

        // Get badges
        userBadgeRepository.findByUserId(currentUser.getId())
                .forEach(userBadge -> activities.add(
                        ActivityLogResponse.builder()
                                .type("badge")
                                .name(userBadge.getBadge().getName())
                                .date(userBadge.getDateObtained())
                                .build()
                ));

        // Sort by date in descending order (most recent first)
        activities.sort(Comparator.comparing(ActivityLogResponse::getDate).reversed());

        return activities;
    }
}
