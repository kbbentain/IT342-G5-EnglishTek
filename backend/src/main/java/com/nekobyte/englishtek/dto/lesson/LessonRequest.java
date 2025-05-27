package com.nekobyte.englishtek.dto.lesson;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class LessonRequest {
    @NotNull
    private Long chapterId;

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotEmpty
    private List<String> content;
}
