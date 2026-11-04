package com.diversestudio.openugc.service;

import com.diversestudio.openugc.dto.AuthRequest;
import com.diversestudio.openugc.dto.AuthResponse;
import com.diversestudio.openugc.dto.RegisterRequest;
import com.diversestudio.openugc.entities.Role;
import com.diversestudio.openugc.entities.User;
import com.diversestudio.openugc.exeption.IncorrectPasswordException;
import com.diversestudio.openugc.repository.RoleRepository;
import com.diversestudio.openugc.repository.UserRepository;
import com.diversestudio.openugc.security.JwtUtil;
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
    private final UserService userService;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, JwtUtil jwtUtil,
                       PasswordEncoder passwordEncoder, UserService userService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
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

    public String register(RegisterRequest request) {
        // Self-registration always gets the USER role; validation and entity building live in UserService.
        userService.createUser(request.username(), request.credential(), "USER");
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
