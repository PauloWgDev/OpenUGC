package com.diversestudio.unityapi.service;

import com.diversestudio.unityapi.dto.AuthRequest;
import com.diversestudio.unityapi.dto.AuthResponse;
import com.diversestudio.unityapi.entities.Role;
import com.diversestudio.unityapi.entities.User;
import com.diversestudio.unityapi.exeption.IncorrectPasswordException;
import com.diversestudio.unityapi.exeption.InvalidPasswordException;
import com.diversestudio.unityapi.exeption.UsernameAlreadyExistsException;
import com.diversestudio.unityapi.repository.RoleRepository;
import com.diversestudio.unityapi.repository.UserRepository;
import com.diversestudio.unityapi.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse login(AuthRequest request)
    {
        Optional<User> userOptional = userRepository.findByUsername(request.username());
        if (userOptional.isEmpty() || !passwordEncoder.matches(request.credential(), userOptional.get().getCredential())) {
            throw new IncorrectPasswordException("Incorrect credentials");
        }

        String token = jwtUtil.generateToken(userOptional.get().getUserId(), userOptional.get().getUsername(), Collections.singletonList(userOptional.get().getRole().getRoleName()));
        return new AuthResponse(token);
    }

    public String register(User user) {

        // Check if the username already exists
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }

        // Set joinedAt to current timestamp if not provided
        if (user.getJoinedAt() == null) {
            user.setJoinedAt(new Timestamp(System.currentTimeMillis()));
        }

        // Set role to USER
        Role defaultRole = roleRepository.findByRoleName("USER")
                .orElseThrow(() -> new IllegalStateException("Default role not found"));
        user.setRole(defaultRole);

        user.setCredential(passwordEncoder.encode(user.getCredential())); // Encrypt password
        userRepository.save(user);
        return "User registered successfully!";
    }


    public AuthResponse guestLogin()
    {
        Role guestRole = roleRepository.findByRoleName("GUEST")
                .orElseThrow(() ->
                        new IllegalStateException("Guest role not found"));

        User guest = new User();

        guest.setUsername(generateGuestUsername());
        guest.setCredential(passwordEncoder.encode(UUID.randomUUID().toString())); // o un valor aleatorio
        guest.setRole(guestRole);
        guest.setJoinedAt(new Timestamp(System.currentTimeMillis()));

        userRepository.save(guest);

        String token = jwtUtil.generateToken(
                guest.getUserId(),
                guest.getUsername(),
                Collections.singletonList(guest.getRole().getRoleName())
        );

        return new AuthResponse(token);
    }

    private String generateGuestUsername() {

        String username;

        do {
            username = "guest_" + UUID.randomUUID().toString().substring(0, 8);
        } while (userRepository.findByUsername(username).isPresent());

        return username;
    }

    public boolean authenticate(String username, String rawPassword) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.isPresent() && passwordEncoder.matches(rawPassword, user.get().getCredential());
    }
}
