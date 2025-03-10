package com.diversestudio.unityapi.service;

import com.diversestudio.unityapi.dto.AuthRequest;
import com.diversestudio.unityapi.dto.AuthResponse;
import com.diversestudio.unityapi.entities.User;
import com.diversestudio.unityapi.exeption.UsernameAlreadyExistsException;
import com.diversestudio.unityapi.repository.UserRepository;
import com.diversestudio.unityapi.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse login(AuthRequest request)
    {
        Optional<User> userOptional = userRepository.findByUsername(request.username());
        if (userOptional.isEmpty() || !passwordEncoder.matches(request.credential(), userOptional.get().getCredential())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(userOptional.get().getUsername());
        return new AuthResponse(token);
    }

    public String register(User user) {

        // Check if the username already exists
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }

        user.setCredential(passwordEncoder.encode(user.getCredential())); // Encrypt password
        userRepository.save(user);
        return "User registered successfully!";
    }

    public boolean authenticate(String username, String rawPassword) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.isPresent() && passwordEncoder.matches(rawPassword, user.get().getCredential());
    }
}
