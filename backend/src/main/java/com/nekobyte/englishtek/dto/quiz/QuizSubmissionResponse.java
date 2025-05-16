package com.nekobyte.englishtek.dto.quiz;

import com.nekobyte.englishtek.dto.badge.BadgeResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuizSubmissionResponse {
    private Integer score;
    private Integer maxScore;
    private Boolean isEligibleForRetake;
    private Boolean isEligibleForBadge;
    private BadgeResponse badge;
    private boolean badgeAwarded;
}
