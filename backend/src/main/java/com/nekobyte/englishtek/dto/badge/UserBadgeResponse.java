package com.nekobyte.englishtek.dto.badge;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserBadgeResponse {
    private Long id;
    private String name;
    private String description;
    private String iconUrl;
    private LocalDateTime dateObtained;
}
