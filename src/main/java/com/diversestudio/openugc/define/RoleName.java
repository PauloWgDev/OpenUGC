package com.diversestudio.openugc.define;

public enum RoleName {
    ADMIN(1),
    USER(2);

    private final int id;

    RoleName(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
