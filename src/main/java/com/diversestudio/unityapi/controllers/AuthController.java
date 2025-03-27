package com.diversestudio.unityapi.controllers;


import com.diversestudio.unityapi.dto.AuthRequest;
import com.diversestudio.unityapi.dto.AuthResponse;
import com.diversestudio.unityapi.entities.User;
import com.diversestudio.unityapi.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    /**
     * POST /api/auth/register - Registers a new user in the system.
     *
     * @param user the {@link User} object containing user registration details
     * @return a {@link ResponseEntity} with a success message or error description
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        return ResponseEntity.ok(authService.register(user));
    }

    /**
     * POST /api/auth/login - Authenticates a user and returns an authentication token if valid.
     *
     * @param request the {@link AuthRequest} containing login credentials (e.g., username and password)
     * @return a {@link ResponseEntity} containing an {@link AuthResponse} with the authentication token
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
