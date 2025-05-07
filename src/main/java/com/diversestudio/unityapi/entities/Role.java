package com.diversestudio.unityapi.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "roles")
@Getter
@Setter
public class Role {
    @Id
    @Column(name = "role_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment
    private int roleId;

    @Column(name = "role_name", nullable = false)
    private String roleName;
}
