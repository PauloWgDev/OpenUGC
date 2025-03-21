package com.diversestudio.unityapi.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "rating")
@Getter
@Setter
@IdClass(RatingId.class)
public class Rating {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "content_id")
    private Long contentId;

    @Column(name = "rating")
    private float rating;

    @Column(name = "comment")
    private String comment;
}
