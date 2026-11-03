package com.diversestudio.unityapi.controllers;

import com.diversestudio.unityapi.dto.CreateUserRequest;
import com.diversestudio.unityapi.dto.UserDTO;
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

    /**
     * POST /api/admin/users - Creates a user account. ADMIN only (enforced in SecurityConfig for /api/admin/**).
     * <p>
     * Unlike /api/auth/register, the admin can choose the role. Omitting "role" creates a USER.
     *
     * @return HTTP 201 with the created {@link UserDTO}; 400 for missing fields or an unknown role
     */
    @PostMapping("/users")
    public ResponseEntity<UserDTO> createUser(@RequestBody CreateUserRequest request)
    {
        UserDTO created = userService.createUser(request.username(), request.credential(), request.role());
        return ResponseEntity.status(201).body(created);
    }

    @PatchMapping("/users/{id}/role/{roleName}")
    public ResponseEntity<Object> updateRole(@PathVariable Long id, @PathVariable String roleName)
    {
        UserDTO userDto = userService.updateRole(id, roleName);
        return ResponseEntity.ok(userDto);
    }
}
