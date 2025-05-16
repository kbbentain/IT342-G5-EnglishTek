package com.nekobyte.englishtek.dto.user;

import com.nekobyte.englishtek.model.User.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserByAdminRequest {
    @Size(min = 3, max = 50)
    private String username;
    
    @Email
    private String email;
    
    @Size(min = 3, max = 50)
    private String name;
    
    private Role role;
    
    @Size(max = 20)
    private String bio;
    
    private String avatar;
}
