package com.diversestudio.unityapi.controllers;

import com.diversestudio.unityapi.dto.UserDTO;
import com.diversestudio.unityapi.entities.User;
import com.diversestudio.unityapi.exeption.ResourceNotFoundException;
import com.diversestudio.unityapi.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/admin")
public class AdminController {
    private  final UserService userService;

    public AdminController(UserService userService)
    {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO userDto = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(userDto);
    }
    // admin endpoints here

    @PatchMapping("/users/{id}/role/{roleName}")
    public ResponseEntity<Object> updateRole(@PathVariable Long id, @PathVariable String roleName)
    {
        UserDTO userDto = userService.updateRole(id, roleName);
        return ResponseEntity.ok(userDto);
    }
}
