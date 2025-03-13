package com.diversestudio.unityapi.controllers;

import com.diversestudio.unityapi.dto.UserDTO;
import com.diversestudio.unityapi.exeption.ResourceNotFoundException;
import com.diversestudio.unityapi.repository.UserRepository;
import com.diversestudio.unityapi.entities.User;
import com.diversestudio.unityapi.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private  final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }


    // Retrieve a single user by ID
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO userDto = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(userDto);
    }


    // Create a new user (registration)
    @PostMapping
    public ResponseEntity<Object> createUser(@RequestBody User user) {
        if (userService.getUserByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.status(400).body("Username already taken");
        }
        UserDTO savedUser = userService.createUser(user);
        return ResponseEntity.status(201).body(savedUser);
    }
}
