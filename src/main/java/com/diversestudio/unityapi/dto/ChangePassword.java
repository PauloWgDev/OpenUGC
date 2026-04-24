package com.diversestudio.unityapi.dto;

public record ChangePassword(
        String currentPassword,
        String newPassword
)
{ }
