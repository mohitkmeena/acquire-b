package com.acquireindia.controller;

import com.acquireindia.dto.ApiResponse;
import com.acquireindia.dto.AuthResponse;
import com.acquireindia.dto.LoginRequest;
import com.acquireindia.dto.RegisterRequest;
import com.acquireindia.model.User;
import com.acquireindia.security.JwtUtil;
import com.acquireindia.service.EmailService;
import com.acquireindia.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            if (userService.existsByEmail(registerRequest.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Email is already taken!"));
            }

            // Create new user
            User user = new User();
            user.setName(registerRequest.getName());
            user.setEmail(registerRequest.getEmail());
            user.setPassword(registerRequest.getPassword());
            user.setRole(registerRequest.getRole());
            user.setPhone(registerRequest.getPhone());
            user.setCompanyName(registerRequest.getCompanyName());

            String encodedPassword = passwordEncoder.encode(user.getPassword());
            User savedUser = userService.createUser(user, encodedPassword);

            // Generate JWT token
            String jwt = jwtUtil.generateToken(savedUser);

            AuthResponse authResponse = new AuthResponse(jwt, savedUser);

            // Send welcome email
            emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getName());

            return ResponseEntity.ok(ApiResponse.success("User registered successfully", authResponse));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            User user = (User) authentication.getPrincipal();
            String jwt = jwtUtil.generateToken(user);

            AuthResponse authResponse = new AuthResponse(jwt, user);

            return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));

        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid email or password"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Login failed: " + e.getMessage()));
        }
    }

    @PostMapping("/demo-login")
    public ResponseEntity<ApiResponse<AuthResponse>> demoLogin(@RequestParam String role) {
        try {
            String demoEmail;
            switch (role.toUpperCase()) {
                case "ADMIN":
                    demoEmail = "admin@acquireindia.com";
                    break;
                case "SELLER":
                    demoEmail = "seller@acquireindia.com";
                    break;
                case "BUYER":
                default:
                    demoEmail = "buyer@acquireindia.com";
                    break;
            }

            User user = userService.findByEmail(demoEmail).orElse(null);
            if (user == null) {
                // Create demo user if doesn't exist
                user = new User();
                user.setName("Demo " + role);
                user.setEmail(demoEmail);
                user.setPassword("demo123");
                user.setRole(User.Role.valueOf(role.toUpperCase()));
                user.setKycStatus(User.KycStatus.APPROVED);

                String encodedPassword = passwordEncoder.encode(user.getPassword());
                user = userService.createUser(user, encodedPassword);
            }

            String jwt = jwtUtil.generateToken(user);

            AuthResponse authResponse = new AuthResponse(jwt, user);

            return ResponseEntity.ok(ApiResponse.success("Demo login successful", authResponse));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Demo login failed: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        // In a stateless JWT implementation, logout is handled client-side
        // by removing the token. Server-side logout would require token blacklisting.
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refreshToken(Authentication authentication) {
        try {
            User user = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String newToken = jwtUtil.generateToken(user);

            Map<String, String> response = new HashMap<>();
            response.put("token", newToken);

            return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to refresh token: " + e.getMessage()));
        }
    }
}