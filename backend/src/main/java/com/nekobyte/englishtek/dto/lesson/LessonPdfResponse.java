package com.nekobyte.englishtek.dto.lesson;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LessonPdfResponse {
    private byte[] content;
    private String fileName;
}
