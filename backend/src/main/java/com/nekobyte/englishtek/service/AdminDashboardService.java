package com.nekobyte.englishtek.service;

import com.nekobyte.englishtek.dto.dashboard.AdminDashboardResponse;
import com.nekobyte.englishtek.model.LessonAttempt;
import com.nekobyte.englishtek.model.QuizAttempt;
import com.nekobyte.englishtek.model.User;
import com.nekobyte.englishtek.model.UserBadge;
import com.nekobyte.englishtek.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {
    private final UserRepository userRepository;
    private final ChapterRepository chapterRepository;
    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final LessonAttemptRepository lessonAttemptRepository;

    public AdminDashboardResponse getDashboardStats() {
        // Get basic counts
        int totalUsers = (int) userRepository.count();
        int totalChapters = (int) chapterRepository.count();
        int totalLessons = (int) lessonRepository.count();
        int totalQuizzes = (int) quizRepository.count();
        int totalBadgesGiven = (int) userBadgeRepository.count();

        // Get top 10 scorers
        List<AdminDashboardResponse.UserScoreDto> topScorers = getTopScorers();

        // Get activity by day for the last 30 days
        Map<String, Integer> activityByDay = getActivityByDay();

        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalChapters(totalChapters)
                .totalLessons(totalLessons)
                .totalQuizzes(totalQuizzes)
                .totalBadgesGiven(totalBadgesGiven)
                .topScorers(topScorers)
                .activityByDay(activityByDay)
                .build();
    }

    private List<AdminDashboardResponse.UserScoreDto> getTopScorers() {
        // Get all users and their quiz attempts
        List<User> users = userRepository.findAll();
        Map<User, Integer> userScores = new HashMap<>();

        // Calculate total score for each user
        for (User user : users) {
            int totalScore = quizAttemptRepository.findByUserId(user.getId()).stream()
                    .filter(attempt -> attempt.getCompletedAt() != null)
                    .mapToInt(QuizAttempt::getScore)
                    .sum();
            userScores.put(user, totalScore);
        }

        // Sort users by score and get top 10
        return userScores.entrySet().stream()
                .sorted(Map.Entry.<User, Integer>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    User user = entry.getKey();
                    String avatarUrl = user.getAvatar() != null ? "/api/v1/files/" + user.getAvatar() : null;
                    return AdminDashboardResponse.UserScoreDto.builder()
                            .userId(user.getId())
                            .username(user.getUsername())
                            .avatarUrl(avatarUrl)
                            .totalScore(entry.getValue())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private Map<String, Integer> getActivityByDay() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(29); // Get 30 days including today
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Initialize all days with 0 count
        Map<String, Integer> activityByDay = new TreeMap<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            activityByDay.put(date.format(formatter), 0);
        }

        // Get all activities
        Stream<LocalDateTime> allActivities = Stream.concat(
                Stream.concat(
                    lessonAttemptRepository.findAll().stream()
                        .filter(attempt -> attempt.getCompletedAt() != null)
                        .map(LessonAttempt::getCompletedAt),
                    quizAttemptRepository.findAll().stream()
                        .filter(attempt -> attempt.getCompletedAt() != null)
                        .map(QuizAttempt::getCompletedAt)
                ),
                userBadgeRepository.findAll().stream()
                    .map(UserBadge::getDateObtained)
        );

        // Count activities per day
        allActivities
                .filter(dateTime -> !dateTime.toLocalDate().isBefore(startDate) && !dateTime.toLocalDate().isAfter(endDate))
                .forEach(dateTime -> {
                    String dateStr = dateTime.toLocalDate().format(formatter);
                    activityByDay.merge(dateStr, 1, Integer::sum);
                });

        return activityByDay;
    }
}
