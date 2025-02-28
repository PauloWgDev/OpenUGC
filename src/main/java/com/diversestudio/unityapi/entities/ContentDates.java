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
    private Long content_id;

    @Column(nullable = false, updatable = false)
    private Timestamp created_at;

    @Column(nullable = false)
    private Timestamp updated_at;


    @OneToOne
    @MapsId // Ensures content_id is the same as in Content
    @JoinColumn(name = "content_id")
    @JsonBackReference
    private Content content;
}
