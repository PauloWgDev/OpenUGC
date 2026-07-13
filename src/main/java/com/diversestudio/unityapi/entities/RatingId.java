package com.diversestudio.unityapi.entities;

import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode
public class RatingId implements Serializable {
    private Long userId;
    private Long contentId;
}
