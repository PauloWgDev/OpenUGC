package com.diversestudio.unityapi.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "content")
@Getter
@Setter
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment
    private Long contentId;

    @Column(nullable = false)
    private String data;

    @Column
    private String thumbnail;

    @ManyToOne
    @JoinColumn(name = "creator", referencedColumnName = "user_id", nullable = false)
    private User creator;

    @Column(nullable = false, length = 100)
    private String name;

    @Column
    private String description;

    @Column
    private int version;

    @OneToOne(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private ContentDates contentDates;
}
