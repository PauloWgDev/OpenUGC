package com.diversestudio.unityapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ContentCreationDTO(
        Long creatorId,
        String name,
        String description,
        int version,
        String data,
        String thumbnail,
        List<String> tags
)
{ }
