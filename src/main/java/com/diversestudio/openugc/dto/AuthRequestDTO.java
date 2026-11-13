package com.diversestudio.openugc.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequestDTO(
        @NotBlank String username,
        @NotBlank String credential
) { }
