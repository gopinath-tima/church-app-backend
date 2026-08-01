package com.church.churchapp.service;

import com.church.churchapp.entity.User;
import com.church.churchapp.enums.Role;
import com.church.churchapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User createUser(String username, String password, Set<Role> requestedRoles) {

        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        List<String> currentRoles = authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());

        boolean isSuperPlus = currentRoles.contains("ROLE_SUPER_PLUS_ADMIN");
        boolean isSuper = currentRoles.contains("ROLE_SUPER_ADMIN");
        boolean isAdmin = currentRoles.contains("ROLE_ADMIN");

        // ✅ ADD_MEMBER added to this allowed list
        List<Role> allowedForAdmin = List.of(
                Role.CORE, Role.INVENTORY, Role.FINANCE,
                Role.WORKFORCE, Role.COMMUNICATION, Role.REPORTING,
                Role.ADD_MEMBER
        );

        for (Role requestedRole : requestedRoles) {
            if (isSuperPlus) {
                continue;
            } else if (isSuper) {
                if (requestedRole == Role.SUPER_PLUS_ADMIN || requestedRole == Role.SUPER_ADMIN) {
                    throw new RuntimeException("Security Violation: Super Admin cannot create " + requestedRole);
                }
            } else if (isAdmin) {
                if (!allowedForAdmin.contains(requestedRole)) {
                    throw new RuntimeException("Security Violation: Admin cannot create " + requestedRole);
                }
            } else {
                throw new RuntimeException("Security Violation: Unauthorized");
            }
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(requestedRoles);

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(Long id) {
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (targetUser.getUsername().equals(currentUsername)) {
            throw new RuntimeException("Security Violation: You cannot delete yourself");
        }

        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        List<String> currentRoles = authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());

        boolean isSuperPlus = currentRoles.contains("ROLE_SUPER_PLUS_ADMIN");
        boolean isSuper = currentRoles.contains("ROLE_SUPER_ADMIN");

        if (targetUser.getRoles().contains(Role.SUPER_PLUS_ADMIN)) {
            if (!isSuperPlus) {
                throw new RuntimeException("Security Violation: Only Super Plus Admin can delete a Super Plus Admin user");
            }
        } else if (targetUser.getRoles().contains(Role.SUPER_ADMIN)) {
            if (!isSuperPlus && !isSuper) {
                throw new RuntimeException("Security Violation: Unauthorized to delete Super Admin");
            }
        }

        userRepository.delete(targetUser);
    }

    public User updateUser(Long id, String username, String password, Set<Role> requestedRoles) {
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        List<String> currentRoles = authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());

        boolean isSuperPlus = currentRoles.contains("ROLE_SUPER_PLUS_ADMIN");
        boolean isSuper = currentRoles.contains("ROLE_SUPER_ADMIN");

        if (targetUser.getRoles().contains(Role.SUPER_PLUS_ADMIN) && !isSuperPlus) {
            throw new RuntimeException("Security Violation: Only Super Plus Admin can update a Super Plus Admin");
        }

        if (requestedRoles != null) {
            for (Role r : requestedRoles) {
                if (r == Role.SUPER_PLUS_ADMIN && !isSuperPlus) {
                    throw new RuntimeException("Security Violation: Cannot assign Super Plus Admin role");
                }
            }
            targetUser.setRoles(requestedRoles);
        }

        if (username != null && !username.trim().isEmpty() && !username.equals(targetUser.getUsername())) {
            if (userRepository.findByUsername(username).isPresent()) {
                throw new RuntimeException("Username already exists");
            }
            targetUser.setUsername(username);
        }

        if (password != null && !password.trim().isEmpty()) {
            targetUser.setPassword(passwordEncoder.encode(password));
        }

        return userRepository.save(targetUser);
    }
}