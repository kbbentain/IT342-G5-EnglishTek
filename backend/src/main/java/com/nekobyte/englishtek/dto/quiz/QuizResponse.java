package com.nekobyte.englishtek.dto.quiz;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class QuizResponse {
    private Long id;
    private Long chapterId;
    private String title;
    private String description;
    private Integer difficulty;
    private Integer maxScore;
    private Integer numberOfItems;
    private Long badgeId;
    private Boolean isRandom;
    private List<QuestionResponse> questions;
    private boolean completed;

    @Data
    @Builder
    public static class QuestionResponse {
        private Long id;
        private Integer page;
        private String type;
        private String title;
        private List<String> choices;
        @JsonProperty("correct_answer")
        private Object correctAnswer;
    }
}
