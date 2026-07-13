package com.diversestudio.unityapi.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name = "downloads")
@Getter
@Setter
@IdClass(DownloadId.class)
public class Download {

    @Id
    @Column(name = "content_id")
    private Long contentId;

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "content_version")
    private int contentVersion;

    @Column(name = "date")
    private Timestamp date;

    @Column(name = "ip_address")
    private String ipAddress;
}
