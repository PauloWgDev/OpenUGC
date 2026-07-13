package com.diversestudio.unityapi.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name = "content_dates")
@Getter
@Setter
public class ContentDates {

    @Id
    private Long contentId;

    @Column(nullable = false, updatable = false)
    private Timestamp createdAt;

    @Column(nullable = false)
    private Timestamp updatedAt;

    @Column(nullable = true)
    private Timestamp deletedAt;

    @Column(nullable = true)
    private Timestamp recoveredAt;

    @OneToOne
    @MapsId // Ensures content_id is the same as in Content
    @JoinColumn(name = "content_id")
    @JsonBackReference
    private Content content;
}
