package com.auth.auth_service.controller;

import com.auth.auth_service.model.AppUser;
import com.auth.auth_service.repository.UserRepository;
import com.auth.auth_service.service.JwtService;
import com.auth.auth_service.service.CustomUserDetailsService;

import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
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
        user.getRoles().add("ROLE_USER");

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
    
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {
        // 🔒 This will ONLY exist if user is logged in
        String email = authentication.getName();

        AppUser user = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        
        if (!user.getRoles().contains("ROLE_USER")) {
            return ResponseEntity
                    .status(403)
                    .body("Only normal users can change password");
        }

        if (!encoder.matches(request.getOldPassword(), user.getPassword())) {
            return ResponseEntity
                    .badRequest()
                    .body("Old password is incorrect");
        }

       
        user.setPassword(encoder.encode(request.getNewPassword()));
        repo.save(user);

        return ResponseEntity.ok("Password changed successfully");
    }
}
