package com.church.churchapp.config;

import com.church.churchapp.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        System.out.println("Incoming Request: " + request.getMethod() + " " + request.getRequestURI());

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            System.out.println("Auth token detected. Validating...");

            try {
                Claims claims = jwtUtil.validateToken(token);
                String username = claims.getSubject();
                System.out.println("Token valid. User: " + username);

                // ✅ Safely extract roles with null check
                Object rolesObj = claims.get("roles");
                List<String> roles = null;
                
                if (rolesObj instanceof List) {
                    roles = (List<String>) rolesObj;
                } else if (rolesObj instanceof String) {
                    // Handle legacy string roles if any
                    roles = List.of((String) rolesObj);
                }

                List<SimpleGrantedAuthority> authorities = (roles != null) 
                    ? roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList())
                    : List.of();

                // ✅ Extract branchId if present
                if (claims.containsKey("branchId")) {
                    request.setAttribute("jwtBranchId", claims.get("branchId", Long.class));
                }
                
                request.setAttribute("jwtRoles", roles); // To check super admin status

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authToken);

            } catch (Exception e) {
                System.err.println("JWT Filter ERROR: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("No Bearer token found in headers for: " + request.getRequestURI());
        }
        filterChain.doFilter(request, response);
    }
}