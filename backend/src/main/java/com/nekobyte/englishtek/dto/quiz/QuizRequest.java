package com.nekobyte.englishtek.dto.quiz;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

import java.util.List;

@Data
public class QuizRequest {
    @NotNull
    private Long chapterId;

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    @Min(1)
    @Max(3)
    private Integer difficulty;

    @NotNull
    @Min(1)
    private Integer maxScore;

    private Boolean isRandom;

    private Long badgeId;

    @NotEmpty
    @Valid
    private List<QuestionRequest> questions;

    @Data
    public static class QuestionRequest {
        @NotNull
        private Integer page;

        @NotBlank
        private String type;

        @NotBlank
        private String title;

        @NotEmpty
        private List<String> choices;

        @JsonProperty("correct_answer")
        private Object correctAnswer;
    }
}
