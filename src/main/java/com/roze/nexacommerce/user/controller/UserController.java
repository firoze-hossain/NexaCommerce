
package com.roze.nexacommerce.user.controller;

import com.roze.nexacommerce.common.BaseController;
import com.roze.nexacommerce.common.BaseResponse;
import com.roze.nexacommerce.common.PaginatedResponse;
import com.roze.nexacommerce.user.dto.request.UserRequest;
import com.roze.nexacommerce.user.dto.response.UserResponse;
import com.roze.nexacommerce.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController extends BaseController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_USER')")
    public ResponseEntity<BaseResponse<UserResponse>> createUser(@Valid @RequestBody UserRequest userRequest) {
        UserResponse userResponse = userService.create(userRequest);
        return created(userResponse, "User created Successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ_USER')")
    public ResponseEntity<BaseResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse userResponse = userService.getUserById(id);
        return ok(userResponse, "User retrieved successfully");
    }

    @GetMapping
    @PreAuthorize("hasAuthority('READ_USER')")
    public ResponseEntity<BaseResponse<PaginatedResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PaginatedResponse<UserResponse> users = userService.getAllUsers(pageable);
        return paginated(users, "Users retrieved successfully");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    public ResponseEntity<BaseResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest userRequest) {
        UserResponse userResponse = userService.updateUser(id, userRequest);
        return ok(userResponse, "User updated successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_USER')")
    public ResponseEntity<BaseResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return noContent("User deleted successfully");
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    public ResponseEntity<BaseResponse<UserResponse>> deactivateUser(@PathVariable Long id) {
        UserResponse userResponse = userService.deactivateUser(id);
        return ok(userResponse, "User deactivated successfully");
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    public ResponseEntity<BaseResponse<UserResponse>> activateUser(@PathVariable Long id) {
        UserResponse userResponse = userService.activateUser(id);
        return ok(userResponse, "User activated successfully");
    }
}