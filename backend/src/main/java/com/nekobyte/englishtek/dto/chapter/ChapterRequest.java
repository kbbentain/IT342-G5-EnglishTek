package com.nekobyte.englishtek.dto.chapter;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ChapterRequest {
    @NotBlank
    private String title;
    
    @NotBlank
    private String description;
    
    private MultipartFile icon;
}
