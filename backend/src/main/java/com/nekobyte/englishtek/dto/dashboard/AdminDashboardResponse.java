package com.nekobyte.englishtek.dto.dashboard;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class AdminDashboardResponse {
    private int totalUsers;
    private int totalChapters;
    private int totalLessons;
    private int totalQuizzes;
    private int totalBadgesGiven;
    private List<UserScoreDto> topScorers;
    private Map<String, Integer> activityByDay;

    @Data
    @Builder
    public static class UserScoreDto {
        private Long userId;
        private String username;
        private String avatarUrl;
        private int totalScore;
    }
}
