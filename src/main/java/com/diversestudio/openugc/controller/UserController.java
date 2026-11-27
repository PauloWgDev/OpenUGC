package com.diversestudio.openugc.controller;

import com.diversestudio.openugc.config.ApiPaths;
import jakarta.validation.Valid;

import com.diversestudio.openugc.dto.ChangePasswordDTO;
import com.diversestudio.openugc.dto.UserDTO;
import com.diversestudio.openugc.exception.ResourceNotFoundException;
import com.diversestudio.openugc.security.AuthHelper;
import com.diversestudio.openugc.service.UserService;
import com.diversestudio.openugc.util.PageRequests;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(ApiPaths.V1 + "/users")
public class UserController {

    private  final UserService userService;
    private final PageRequests pageRequests;

    public UserController(UserService userService, PageRequests pageRequests)
    {
        this.userService = userService;
        this.pageRequests = pageRequests;
    }

    /**
     * GET /api/v1/users - Retrieves a paginated list of users, optionally filtered by a prompt
     * and sorted by a specified field and direction.
     *
     * @param prompt optional filter string to match against user data (default: empty string)
     * @param page the page number to retrieve (default: 0)
     * @param size the number of users per page (default: 10)
     * @param sort the sort criteria in the format "property,direction" (e.g., "joinedAt,desc")
     * @return a {@link ResponseEntity} containing a page of {@link UserDTO} objects
     */
    @GetMapping
    public ResponseEntity<PagedModel<UserDTO>> getUserPage(
            @RequestParam(defaultValue = "") String prompt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "joinedAt,desc") String sort
    )
    {
        Pageable pageable = pageRequests.of(page, size, sort);
        Page<UserDTO> userPage = userService.getUserPage(prompt, pageable);
        return ResponseEntity.ok(new PagedModel<>(userPage));
    }


    /**
     * GET /api/v1/users/{id} - Retrieves a specific user by their ID.
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

    // Account creation is not exposed here: players use POST /api/v1/auth/register,
    // and admins use POST /api/v1/admin/users.

    /**
     * PATCH /api/v1/users - Updates the current user's username, profile picture and/or about me.
     * Fields left out (null) are not changed.
     *
     * @return 200 with the updated {@link UserDTO}, so clients don't need another GET
     */
    @PatchMapping()
    public ResponseEntity<UserDTO> updateUserData(@Valid @RequestBody UserDTO user)
    {
        Long userId = AuthHelper.getCurrentUserId();
        userService.patchUser(userId, user);
        UserDTO updated = userService.getUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(updated);
    }

    /**
     * PATCH /api/v1/users/password - Changes the current user's password.
     *
     * @return 204 with no body; 400 if the current password is wrong or the new one breaks the rules
     */
    @PatchMapping("/password")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody ChangePasswordDTO changePasswordObject)
    {
        Long userId = AuthHelper.getCurrentUserId();
        userService.changePassword(userId, changePasswordObject);
        return ResponseEntity.noContent().build();
    }
}
