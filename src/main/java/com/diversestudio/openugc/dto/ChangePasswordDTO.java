package com.diversestudio.openugc.dto;

public record ChangePasswordDTO(
        String currentPassword,
        String newPassword
)
{ }
