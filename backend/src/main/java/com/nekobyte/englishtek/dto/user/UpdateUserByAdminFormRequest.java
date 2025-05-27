package com.nekobyte.englishtek.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserByAdminFormRequest {
    private String username;
    private String email;
    private String name;
    private String role;  // "USER" or "ADMIN"
    
    @Size(max = 20, message = "Bio cannot exceed 20 characters")
    private String bio;
}
