package com.nekobyte.englishtek.dto.badge;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BadgeRequest {
    @NotBlank
    private String name;
    
    @NotBlank
    private String description;
    
    @NotBlank
    private String iconUrl;
}
