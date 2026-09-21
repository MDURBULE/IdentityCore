package com.IdentityCore.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.IdentityCore.config.Config;
import com.IdentityCore.model.request.LoginRequest;
import com.IdentityCore.model.request.RefreshTokenRequest;
import com.IdentityCore.model.request.RegisterRequest;
import com.IdentityCore.model.response.AuthResponse;
import com.IdentityCore.model.response.RegisterResponse;
import com.IdentityCore.model.response.StatusResponse;
import com.IdentityCore.service.Interface.AuthService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request, HttpServletRequest req) {
        AuthService.RegisterResult result = authService.registerUser(request, null, null, null);

        return ResponseEntity.ok(new RegisterResponse(
                result.publicId(),
                result.email(),
                result.status(),
                "Registration successful. Please check your email to verify your account."));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request,HttpServletRequest req) {
        AuthService.LoginResult result = authService.login(
                request.email(),
                request.password(),
                request.clientId() != null ? request.clientId() : Config.getCpx().getAppName(),
                "Browser",

                req.getRemoteAddr(),
                req.getHeader("User-Agent"));
        return ResponseEntity.ok(null);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(null);
    }

    @PostMapping("/logout")
    public ResponseEntity<StatusResponse> logout(@RequestBody AuthenticatedPrincipal request) {
        return ResponseEntity.ok(null);
    }

    @PostMapping("/email/send-verification")
    public ResponseEntity<AuthResponse> sendverification(@RequestBody AuthenticatedPrincipal request) {
        return ResponseEntity.ok(null);
    }

    @PostMapping("/email/verify")
    public ResponseEntity<AuthResponse> verifyEmail(@RequestBody AuthenticatedPrincipal request) {
        return ResponseEntity.ok(null);
    }

    @PostMapping("/password/forgot")
    public ResponseEntity<AuthResponse> forgotPassword(@RequestBody AuthenticatedPrincipal request) {
        return ResponseEntity.ok(null);
    }

    @PostMapping("/password/reset")
    public ResponseEntity<AuthResponse> resetPassword(@RequestBody AuthenticatedPrincipal request) {
        return ResponseEntity.ok(null);
    }
}
