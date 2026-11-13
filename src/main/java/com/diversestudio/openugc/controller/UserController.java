package com.diversestudio.openugc.controller;

import jakarta.validation.Valid;

import com.diversestudio.openugc.dto.ChangePasswordDTO;
import com.diversestudio.openugc.dto.UserDTO;
import com.diversestudio.openugc.exception.ResourceNotFoundException;
import com.diversestudio.openugc.security.AuthHelper;
import com.diversestudio.openugc.service.UserService;
import com.diversestudio.openugc.util.NativeQueryHelper;
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

    // Account creation is not exposed here: players use POST /api/auth/register,
    // and admins use POST /api/admin/users.

    @PatchMapping()
    public ResponseEntity<Object> updateUserData(@Valid @RequestBody UserDTO user)
    {
        Long userId = AuthHelper.getCurrentUserId();
        userService.patchUser(userId, user);
        return ResponseEntity.status(200).body("User Patched Successfully");
    }

    @PatchMapping("/password")
    public ResponseEntity<Object> updatePassword(@Valid @RequestBody ChangePasswordDTO changePasswordObject)
    {
        Long userId = AuthHelper.getCurrentUserId();

        userService.changePassword(userId, changePasswordObject);

        return ResponseEntity.status(200).body("Password changed successfully");
    }
}
