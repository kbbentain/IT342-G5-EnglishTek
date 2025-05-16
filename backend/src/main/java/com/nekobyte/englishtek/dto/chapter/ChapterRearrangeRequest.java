package com.nekobyte.englishtek.dto.chapter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ChapterRearrangeRequest {
    @NotEmpty(message = "Order list cannot be empty")
    private List<@Valid ChapterItemOrder> order;

    @Data
    public static class ChapterItemOrder {
        @NotNull(message = "Item ID cannot be null")
        private Long id;

        @NotNull(message = "Item type cannot be null")
        private String type;
    }
}
