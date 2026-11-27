package com.diversestudio.openugc.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class RatingId implements Serializable {
    private Long userId;
    private Long contentId;
}
