package com.diversestudio.unityapi.entities;


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
    @MapsId
    @JoinColumn(name = "content_id")
    private Content content;
}
