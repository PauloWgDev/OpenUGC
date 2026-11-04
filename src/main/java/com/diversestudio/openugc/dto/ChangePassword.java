package com.diversestudio.openugc.dto;

public record ChangePassword(
        String currentPassword,
        String newPassword
)
{ }
