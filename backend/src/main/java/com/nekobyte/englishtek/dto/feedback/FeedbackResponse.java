package com.nekobyte.englishtek.dto.feedback;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FeedbackResponse {
    private Long id;
    private Long userId;
    private String username;
    private Long chapterId;
    private String chapterTitle;
    private Integer rating;
    private String feedbackText;
    private String feedbackKeyword;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
