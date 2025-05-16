package com.nekobyte.englishtek.dto.chapter;

import com.nekobyte.englishtek.model.ChapterStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterListResponse {
    private Long id;
    private String icon;
    private String title;
    private int totalTasks;
    private int completedTasks;
    private double progressPercentage;
    private ChapterStatus status;
}
