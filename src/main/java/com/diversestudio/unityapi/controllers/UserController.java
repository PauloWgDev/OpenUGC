package com.diversestudio.unityapi.controllers;

import com.diversestudio.unityapi.repository.UserRepository;
import com.diversestudio.unityapi.entities.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    // Retrieve all users (just for testing)
    @GetMapping
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    // Retrieve a single user by ID
    @GetMapping("/{id}")
    public Optional<ResponseEntity<User>> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(ResponseEntity::ok);
    }


    // Create a new user
    @PostMapping
    public ResponseEntity<Object> createUser(@RequestBody User user) {
        // Check if username already exists
        Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser.isPresent()) {
            return ResponseEntity.status(400).body("Username already taken");
        }

        User savedUser = userRepository.save(user);
        return ResponseEntity.status(201).body(savedUser); // 201 Created
    }
}
