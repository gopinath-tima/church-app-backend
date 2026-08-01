package com.church.churchapp.service;

import com.church.churchapp.entity.User;
import com.church.churchapp.repository.UserRepository;
import com.church.churchapp.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public String authenticate(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        Long branchId = user.getBranch() != null ? user.getBranch().getId() : null;

        // ✅ Updated: Passes roles and branchId to the token
        return jwtUtil.generateToken(user.getUsername(), user.getRoles(), branchId);
    }
}