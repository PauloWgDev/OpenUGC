package com.diversestudio.openugc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordDTO(
        @NotBlank String currentPassword,
        @NotBlank @Size(min = UserLimits.PASSWORD_MIN, max = UserLimits.PASSWORD_MAX) String newPassword
)
{ }
