package com.nekobyte.englishtek.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Schema(description = "User's username")
    private String username;

    @Schema(description = "User's name")
    private String name;

    @Schema(description = "User's email address")
    @Email(message = "Invalid email format")
    private String email;

    @Schema(description = "Current password (required for password update)")
    private String existingPassword;

    @Schema(description = "New password (minimum 6 characters)")
    @Size(min = 6, message = "New password must be at least 6 characters", groups = PasswordUpdate.class)
    private String newPassword;

    @Schema(description = "User's bio (maximum 20 characters)")
    @Size(max = 20, message = "Bio must not exceed 20 characters")
    private String bio;

    @Schema(description = "Set to true if updating password")
    private boolean passwordUpdateRequested;

    // Marker interface for password validation group
    public interface PasswordUpdate {}

    public boolean isPasswordUpdateRequested() {
        return newPassword != null && !newPassword.trim().isEmpty();
    }
}
