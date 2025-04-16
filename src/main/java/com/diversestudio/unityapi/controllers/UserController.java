package com.diversestudio.unityapi.controllers;

import com.diversestudio.unityapi.dto.UserDTO;
import com.diversestudio.unityapi.exeption.ResourceNotFoundException;
import com.diversestudio.unityapi.entities.User;
import com.diversestudio.unityapi.service.UserService;
import com.diversestudio.unityapi.util.NativeQueryHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
public class UserController {

    private  final UserService userService;
    private final NativeQueryHelper nativeQueryHelper;

    public UserController(UserService userService, NativeQueryHelper nativeQueryHelper)
    {
        this.userService = userService;
        this.nativeQueryHelper = nativeQueryHelper;
    }

    /**
     * GET /api/users - Retrieves a paginated list of users, optionally filtered by a prompt
     * and sorted by a specified field and direction.
     *
     * @param prompt optional filter string to match against user data (default: empty string)
     * @param page the page number to retrieve (default: 0)
     * @param size the number of users per page (default: 10)
     * @param sort the sort criteria in the format "property,direction" (e.g., "joinedAt,desc")
     * @return a {@link ResponseEntity} containing a page of {@link UserDTO} objects
     */
    @GetMapping
    public ResponseEntity<Page<UserDTO>> getUserPage(
            @RequestParam(defaultValue = "") String prompt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "joinedAt,desc") String sort
    )
    {
        // Convert sort string into Spring's Sort object (you can re-use similar logic as in ContentService)
        Sort sortOrder = nativeQueryHelper.StringToSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        Page<UserDTO> userPage = userService.getUserPage(prompt, pageable);
        return ResponseEntity.ok(userPage);
    }


    /**
     * GET /api/users/{id} - Retrieves a specific user by their ID.
     *
     * @param id the ID of the user to retrieve
     * @return a {@link ResponseEntity} containing the {@link UserDTO}
     * @throws ResourceNotFoundException if the user with the specified ID does not exist
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO userDto = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(userDto);
    }

    /**
     * POST /api/users - Creates a new user account.
     *
     * @param user the {@link User} object containing the registration details
     * @return a {@link ResponseEntity} with HTTP 201 and the saved {@link UserDTO} if successful,
     * or HTTP 400 with an error message if the username is already taken
     */
    @PostMapping
    public ResponseEntity<Object> createUser(@RequestBody User user) {
        if (userService.getUserByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.status(400).body("Username already taken");
        }
        UserDTO savedUser = userService.createUser(user);
        return ResponseEntity.status(201).body(savedUser);
    }
}
