package com.church.churchapp.controller;

import com.church.churchapp.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> request) {
        // Grabs the username and password from the React frontend request
        String username = request.get("username");
        String password = request.get("password");

        // Passes them to the updated AuthService we just fixed
        String token = authService.authenticate(username, password);

        // Sends the JWT token back to React
        return ResponseEntity.ok(token);
    }
}