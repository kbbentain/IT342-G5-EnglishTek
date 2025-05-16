package com.nekobyte.englishtek.dto.chapter;

import com.nekobyte.englishtek.model.ChapterStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterResponse {
    private Long id;
    private String title;
    private String description;
    private String iconUrl;
    private List<ChapterItemDto> items;
    private ChapterStatus status;
    private boolean hasCompletedFeedback;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChapterItemDto {
        private Long id;
        private String type;
        private String title;
        private int order;
        private boolean completed;
    }
}
