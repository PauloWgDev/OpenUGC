package com.diversestudio.unityapi.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


/**
 * A utility class for accessing authentication information from the security context.
 * <p>
 * This class provides helper methods to retrieve details about the currently authenticated user.
 * </p>
 */
public class AuthHelper
{
    // Singleton
    private AuthHelper() {}

    /**
     * Retrieves the current authenticated user's ID.
     * <p>
     * The method obtains the authentication from the security context and parses the user ID from its {@code name}.
     * It assumes that the {@code name} contains a valid user ID in string format.
     * </p>
     *
     * @return the user ID as a {@link Long}.
     * @throws IllegalStateException if the authentication is not available or the user ID cannot be parsed.
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("No authentication found or the user is not authenticated");
        }
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("User ID is not a valid number: " + authentication.getName(), e);
        }
    }
}
