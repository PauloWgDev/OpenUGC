package com.diversestudio.unityapi.controllers;


import com.diversestudio.unityapi.dto.AuthRequest;
import com.diversestudio.unityapi.dto.AuthResponse;
import com.diversestudio.unityapi.entities.User;
import com.diversestudio.unityapi.service.AuthService;
import com.diversestudio.unityapi.service.RateLimiterService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final RateLimiterService rateLimiterService;

    public AuthController(AuthService authService, RateLimiterService rateLimiterService) {
        this.authService = authService;
        this.rateLimiterService = rateLimiterService;
    }
    
    /**
     * POST /api/auth/register - Registers a new user in the system.
     *
     * @param user the {@link User} object containing user registration details
     * @return a {@link ResponseEntity} with a success message or error description
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user, HttpServletRequest httpRequest) {
        String clientIp = httpRequest.getRemoteAddr();
        Bucket bucket = rateLimiterService.resolveBucket("register:" + clientIp);

        //return ResponseEntity.ok(authService.register(user));
        if (bucket.tryConsume(1)) {
            return ResponseEntity.ok(authService.register(user));
        } else {
            return ResponseEntity.status(429).body("Too many registration attempts. Please try again later.");
        }
    }

    /**
     * POST /api/auth/login - Authenticates a user and returns an authentication token if valid.
     *
     * @param request the {@link AuthRequest} containing login credentials (e.g., username and password)
     * @return a {@link ResponseEntity} containing an {@link AuthResponse} with the authentication token
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request, HttpServletRequest httpRequest) {
        String clientIp = httpRequest.getRemoteAddr(); // or getHeader("X-Forwarded-For") for proxies
        Bucket bucket = rateLimiterService.resolveBucket("login:" + clientIp);

        if (bucket.tryConsume(1)) {
            return ResponseEntity.ok(authService.login(request));
        } else {
            return ResponseEntity.status(429).body("Too many login attempts. Please try again later.");
        }
    }
}
