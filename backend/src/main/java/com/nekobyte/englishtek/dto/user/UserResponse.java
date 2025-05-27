package com.nekobyte.englishtek.dto.user;

import com.nekobyte.englishtek.model.User.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String name;
    private Role role;
    private String bio;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private int totalBadges;
    private int completedChapters;
    private int completedLessons;
    private int completedQuizzes;
}
