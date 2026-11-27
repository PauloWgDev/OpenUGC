package com.diversestudio.openugc.controller;

import com.diversestudio.openugc.config.ApiPaths;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;

import com.diversestudio.openugc.config.GuestProperties;
import com.diversestudio.openugc.dto.AuthRequestDTO;
import com.diversestudio.openugc.dto.AuthResponseDTO;
import com.diversestudio.openugc.dto.RegisterRequestDTO;
import com.diversestudio.openugc.dto.UserDTO;
import com.diversestudio.openugc.exception.UnauthorizedActionException;
import com.diversestudio.openugc.service.AuthService;
import com.diversestudio.openugc.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPaths.V1 + "/auth")
@SecurityRequirements // Public: no token needed (shown without a lock in Swagger UI)
public class AuthController {
    private final AuthService authService;
    private final RateLimiterService rateLimiterService;
    private final GuestProperties guestProperties;

    public AuthController(AuthService authService, RateLimiterService rateLimiterService, GuestProperties guestProperties) {
        this.authService = authService;
        this.rateLimiterService = rateLimiterService;
        this.guestProperties = guestProperties;
    }
    
    /**
     * POST /api/v1/auth/register - Registers a new user in the system, always with the USER role.
     *
     * @param request the {@link RegisterRequestDTO} with the new username and password
     * @return 201 with the created user; 429 (ProblemDetail, Retry-After) if the IP is over the limit
     */
    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterRequestDTO request, HttpServletRequest httpRequest) {
        rateLimiterService.consume("register:" + httpRequest.getRemoteAddr(),
                "Too many registration attempts. Please try again later.");
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    /**
     * POST /api/v1/auth/login - Authenticates a user and returns an authentication token if valid.
     *
     * @param request the {@link AuthRequestDTO} containing login credentials (e.g., username and password)
     * @return 200 with an {@link AuthResponseDTO}; 401 for wrong credentials; 429 if the IP is over the limit
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO request, HttpServletRequest httpRequest) {
        rateLimiterService.consume("login:" + httpRequest.getRemoteAddr(),
                "Too many login attempts. Please try again later.");
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * POST /api/v1/auth/guest - Creates a guest account and returns its token.
     *
     * @return 200 with an {@link AuthResponseDTO}; 403 if guests are disabled; 429 if the IP is over the limit
     */
    @PostMapping("/guest")
    public ResponseEntity<AuthResponseDTO> guestLogin(HttpServletRequest httpRequest) {
        if (!guestProperties.isEnabled()) {
            throw new UnauthorizedActionException("Guest users are disabled.");
        }
        rateLimiterService.consume("guest:" + httpRequest.getRemoteAddr(),
                "Too many guest login attempts. Please try again later.");
        return ResponseEntity.ok(authService.guestLogin());
    }
}
