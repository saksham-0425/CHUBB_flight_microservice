package com.auth.auth_service.controller;

import com.auth.auth_service.model.AppUser;
import com.auth.auth_service.repository.UserRepository;
import com.auth.auth_service.service.JwtService;
import com.auth.auth_service.service.CustomUserDetailsService;

import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.auth.auth_service.dto.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder encoder;
    private final UserRepository repo;
    private final JwtService jwtService;

    public AuthController(
            AuthenticationManager authManager,
            CustomUserDetailsService userDetailsService,
            PasswordEncoder encoder,
            UserRepository repo,
            JwtService jwtService) {

        this.authManager = authManager;
        this.userDetailsService = userDetailsService;
        this.encoder = encoder;
        this.repo = repo;
        this.jwtService = jwtService;
    }

    // REGISTER
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

        if (repo.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("User already exists");
        }

        AppUser user = new AppUser();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(encoder.encode(request.getPassword()));
        user.getRoles().add(request.getRole() != null ? request.getRole() : "ROLE_USER");

        repo.save(user);
        return ResponseEntity.status(201).body("Registered successfully");
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserDetails user = userDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(new AuthResponse(token));
    }
}
