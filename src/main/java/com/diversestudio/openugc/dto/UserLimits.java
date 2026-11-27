package com.diversestudio.openugc.dto;

/**
 * Length limits for account fields, shared by the request DTOs so register, admin create,
 * PATCH /api/v1/users (including profilePicture) and password change all enforce the same rules.
 */
public final class UserLimits {
    public static final int USERNAME_MIN = 3;
    public static final int USERNAME_MAX = 32;

    public static final int PASSWORD_MIN = 8;
    public static final int PASSWORD_MAX = 72; // More characters on the password would just be ignored by the encrypter

    public static final int ABOUT_ME_MAX = 2_000;

    // Same as the profile_picture column (varchar(255)), so a longer value is a 400, not a database error.
    public static final int PROFILE_PICTURE_MAX = 255;
    
    private UserLimits() { }
}
