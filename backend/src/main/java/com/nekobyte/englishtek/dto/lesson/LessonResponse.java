package com.nekobyte.englishtek.dto.lesson;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LessonResponse {
    private Long id;
    private Long chapterId;
    private String title;
    private String description;
    private List<String> content;
    private boolean completed;
}
