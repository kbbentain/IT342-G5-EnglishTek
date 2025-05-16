package com.nekobyte.englishtek.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String username;
    private String email;
    private String name;
    private String role;
    private String avatarUrl;
    private String bio;
    private int totalCompletedTasks;
    private int totalBadges;
}
