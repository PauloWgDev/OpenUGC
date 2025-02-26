package com.diversestudio.unityapi;

import com.diversestudio.unityapi.entities.User;
import com.diversestudio.unityapi.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public String register(User user) {
        user.setCredential(passwordEncoder.encode(user.getCredential())); // Encrypt password
        userRepository.save(user);
        return "User registered successfully!";
    }

    public boolean authenticate(String username, String rawPassword) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.isPresent() && passwordEncoder.matches(rawPassword, user.get().getCredential());
    }

}
