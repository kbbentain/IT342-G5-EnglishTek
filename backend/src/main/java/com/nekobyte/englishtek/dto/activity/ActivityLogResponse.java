package com.nekobyte.englishtek.dto.activity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ActivityLogResponse {
    private String type;  // "lesson", "quiz", or "badge"
    private String name;  // name of the lesson, quiz, or badge
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime date;  // completion date
}
