package com.nekobyte.englishtek.dto.feedback;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FeedbackRequest {
    @NotNull
    private Long chapterId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    private String feedbackText;

    @Size(max = 50)
    private String feedbackKeyword;
}
