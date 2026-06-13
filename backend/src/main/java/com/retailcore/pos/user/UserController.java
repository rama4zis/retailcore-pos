package com.retailcore.pos.user;

import com.retailcore.pos.user.dto.UserActiveRequest;
import com.retailcore.pos.user.dto.UserCreateRequest;
import com.retailcore.pos.user.dto.UserResponse;
import com.retailcore.pos.user.dto.UserRoleRequest;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Users", description = "Admin user management endpoints")
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Create user", description = "Creates a user account with a role and active flag.")
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        UserResponse response = userService.create(request);
        return ResponseEntity.created(URI.create("/api/users/" + response.id())).body(response);
    }

    @Operation(summary = "List users", description = "Lists all user accounts.")
    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @Operation(summary = "Change user role", description = "Updates the role assigned to a user.")
    @PatchMapping("/{id}/role")
    public ResponseEntity<UserResponse> changeRole(
            @PathVariable Long id,
            @Valid @RequestBody UserRoleRequest request
    ) {
        return ResponseEntity.ok(userService.changeRole(id, request.role()));
    }

    @Operation(summary = "Change user active status", description = "Enables or disables a user account.")
    @PatchMapping("/{id}/active")
    public ResponseEntity<UserResponse> changeActive(
            @PathVariable Long id,
            @Valid @RequestBody UserActiveRequest request
    ) {
        return ResponseEntity.ok(userService.changeActive(id, request.active()));
    }
}

