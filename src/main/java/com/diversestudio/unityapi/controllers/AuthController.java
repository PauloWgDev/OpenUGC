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

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        return ResponseEntity.ok(authService.register(user));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
