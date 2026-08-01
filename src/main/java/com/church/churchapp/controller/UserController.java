package com.church.churchapp.controller;

import com.church.churchapp.dto.UserRequest;
import com.church.churchapp.dto.UserUpdateRequest;
import com.church.churchapp.entity.User;
import com.church.churchapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('SUPER_PLUS_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
    public User createUser(@Valid @RequestBody UserRequest request) {
        return userService.createUser(
                request.getUsername(),
                request.getPassword(),
                request.getRoles()
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_PLUS_ADMIN', 'SUPER_ADMIN')")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_PLUS_ADMIN', 'SUPER_ADMIN')")
    public User updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return userService.updateUser(
                id,
                request.getUsername(),
                request.getPassword(),
                request.getRoles()
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_PLUS_ADMIN', 'SUPER_ADMIN')")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}