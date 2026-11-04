package com.diversestudio.openugc.entities;

import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode
public class DownloadId implements Serializable {
    private Long contentId;
    private Long userId;
    private int contentVersion;
}
