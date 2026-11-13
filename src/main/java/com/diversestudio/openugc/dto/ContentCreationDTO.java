package com.diversestudio.openugc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ContentCreationDTO(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 10_000) String description,
        @PositiveOrZero int version,
        String data,
        String thumbnail,
        @Size(max = 20) List<@NotBlank @Size(max = 50) String> tags
)
{ }