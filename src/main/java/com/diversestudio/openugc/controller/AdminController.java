package com.diversestudio.openugc.controller;

import com.diversestudio.openugc.dto.CreateUserRequestDTO;
import com.diversestudio.openugc.dto.UserDTO;
import com.diversestudio.openugc.exception.ResourceNotFoundException;
import com.diversestudio.openugc.service.UserService;
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

    /**
     * GET /api/admin/{id} - Retrieves any user by their ID. ADMIN only (enforced in SecurityConfig for /api/admin/**).
     *
     * @param id the ID of the user to retrieve
     * @return HTTP 200 with the {@link UserDTO}
     * @throws ResourceNotFoundException if no user exists with that ID (404)
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO userDto = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(userDto);
    }

    /**
     * POST /api/admin/users - Creates a user account. ADMIN only (enforced in SecurityConfig for /api/admin/**).
     * <p>
     * Unlike /api/auth/register, the admin can choose the role. Omitting "role" creates a USER.
     *
     * @return HTTP 201 with the created {@link UserDTO}; 400 for missing fields or an unknown role
     */
    @PostMapping("/users")
    public ResponseEntity<UserDTO> createUser(@RequestBody CreateUserRequestDTO request)
    {
        UserDTO created = userService.createUser(request.username(), request.credential(), request.role());
        return ResponseEntity.status(201).body(created);
    }

    /**
     * PATCH /api/admin/users/{id}/role/{roleName} - Changes a user's role. ADMIN only (enforced in SecurityConfig for /api/admin/**).
     * <p>
     * The new role applies to permission checks that read the role from the database right away.
     * Tokens the user already holds keep their old role until they expire (up to 1 day).
     *
     * @param id       the ID of the user to update
     * @param roleName the exact role name to assign (ADMIN, USER or GUEST; case-sensitive)
     * @return HTTP 200 with the updated {@link UserDTO}; 404 if the user doesn't exist; 400 if the role doesn't exist
     */
    @PatchMapping("/users/{id}/role/{roleName}")
    public ResponseEntity<Object> updateRole(@PathVariable Long id, @PathVariable String roleName)
    {
        UserDTO userDto = userService.updateRole(id, roleName);
        return ResponseEntity.ok(userDto);
    }
}
