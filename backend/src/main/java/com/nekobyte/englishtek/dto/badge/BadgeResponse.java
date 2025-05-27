package com.nekobyte.englishtek.dto.badge;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BadgeResponse {
    private Long id;
    private String name;
    private String description;
    private String iconUrl;
}
