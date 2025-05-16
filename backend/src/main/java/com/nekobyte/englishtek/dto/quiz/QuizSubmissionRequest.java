package com.nekobyte.englishtek.dto.quiz;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuizSubmissionRequest {
    @NotNull
    @Min(0)
    @Max(100)
    private Integer score;
}
