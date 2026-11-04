package com.diversestudio.openugc.entities;

import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode
public class RatingId implements Serializable {
    private Long userId;
    private Long contentId;
}
