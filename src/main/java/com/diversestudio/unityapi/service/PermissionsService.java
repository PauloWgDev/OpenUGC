package com.diversestudio.unityapi.service;

import com.diversestudio.unityapi.config.GuestProperties;
import com.diversestudio.unityapi.entities.User;
import org.springframework.stereotype.Service;

@Service
public class PermissionsService {
    private final GuestProperties guestProperties;

    public PermissionsService(GuestProperties guestProperties) {
        this.guestProperties = guestProperties;
    }

    public boolean canPost(User user) {

        String role = user.getRole().getRoleName();

        switch (role) {
            case "ADMIN":
            case "USER":
                return true;

            case "GUEST":
                return guestProperties.isEnabled()
                        && guestProperties.isCanPost();

            default:
                return false;
        }
    }
}
