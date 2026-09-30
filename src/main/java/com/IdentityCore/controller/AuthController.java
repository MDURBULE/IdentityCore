package com.IdentityCore.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.IdentityCore.config.Config;
import com.IdentityCore.model.entity.User;
import com.IdentityCore.model.request.AuthenticatedPrincipal;
import com.IdentityCore.model.request.ForgotPasswordRequest;
import com.IdentityCore.model.request.LoginRequest;
import com.IdentityCore.model.request.RefreshTokenRequest;
import com.IdentityCore.model.request.RegisterRequest;
import com.IdentityCore.model.request.ResetPasswordRequest;
import com.IdentityCore.model.response.AuthResponse;
import com.IdentityCore.model.response.RegisterResponse;
import com.IdentityCore.model.response.StatusResponse;
import com.IdentityCore.service.Interface.AuthService;
import com.IdentityCore.service.Interface.PasswordService;
import com.IdentityCore.service.Interface.TokenService;
import com.IdentityCore.service.Interface.UserService;
import com.IdentityCore.utils.Utilities;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final PasswordService passwordService;
    private final TokenService tokenService;

    public AuthController(AuthService authService,
                          UserService userService,
                          PasswordService passwordService,
                          TokenService tokenService) {
        this.authService = authService;
        this.userService = userService;
        this.passwordService = passwordService;
        this.tokenService = tokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest req) {
        AuthService.RegisterResult result = authService.registerUser(request, req.getRemoteAddr(),
                req.getHeader("User-Agent"), Utilities.getBaseUrl(req));

        return ResponseEntity.ok(new RegisterResponse(
                result.publicId(),
                result.email(),
                result.status(),
                "Registration successful. Please check your email to verify your account."));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest req) {
        AuthService.LoginResult result = authService.login(
                request.getEmail(),
                request.getPassword(),
                request.getClientId() != null ? request.getClientId() : Config.getCpx().getAppName(),
                "Browser",
                req.getRemoteAddr(),
                req.getHeader("User-Agent"));

        if (result.mfaRequired()) {
            return ResponseEntity.ok(AuthResponse.ofMfaRequired(result.publicId()));
        }
        return ResponseEntity
                .ok(AuthResponse.ofSuccess(result.publicId(), result.accessToken(), result.refreshToken(), 900));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request, HttpServletRequest req) {
        AuthService.RefreshResult result = authService.refreshToken(
                request.refreshToken(),
                request.clientId() != null ? request.clientId() : Config.getCpx().getAppName(),
                req.getRemoteAddr(),
                req.getHeader("User-Agent"));

        return ResponseEntity.ok(AuthResponse.ofSuccess(null, result.accessToken(), result.refreshToken(), 900));
    }

    @PostMapping("/logout")
    public ResponseEntity<StatusResponse> logout(@AuthenticationPrincipal AuthenticatedPrincipal principal,
            HttpServletRequest req) {
        if (principal != null && principal.getSessionId() != null && !principal.getSessionId().isBlank()) {
            try {
                long sessionId = Long.parseLong(principal.getSessionId());
                userService.findByPublicId(java.util.UUID.fromString(principal.getUserId()))
                        .ifPresent(u -> tokenService.revokeSession(sessionId, u.getId()));
            } catch (Exception e) {
                Config.getLgr().warn("Failed to parse sessionId on logout", e);
            }
        }
        return ResponseEntity.ok(StatusResponse.success("Logged out successfully"));
    }

    @PostMapping("/email/send-verification")
    public ResponseEntity<StatusResponse> sendVerification(@AuthenticationPrincipal AuthenticatedPrincipal principal,
            HttpServletRequest req) {
        if (principal != null) {
            User user = userService.findByPublicId(java.util.UUID.fromString(principal.getUserId()))
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            userService.sendEmailVerification(user, Utilities.getBaseUrl(req));
        }
        return ResponseEntity.ok(new StatusResponse("SENT", "Verification email sent if account exists"));
    }

    @PostMapping("/email/verify")
    public ResponseEntity<StatusResponse> verifyEmail(@RequestParam("token") String token,
            HttpServletRequest req) {
        boolean verified = userService.verifyEmailToken(token, req.getRemoteAddr(), req.getHeader("User-Agent"));
        if (!verified) {
            return ResponseEntity.badRequest().body(StatusResponse.failed("Invalid or expired verification token"));
        }
        return ResponseEntity.ok(StatusResponse.success("Email verified successfully"));
    }

    @PostMapping("/password/forgot")
    public ResponseEntity<StatusResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest req) {
        userService.findByEmail(request.email())
                .ifPresent(user -> passwordService.initiatePasswordReset(user, Utilities.getBaseUrl(req)));
        return ResponseEntity.ok(
                StatusResponse.success("If an account exists for that email, password reset instructions were sent."));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<StatusResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request,
            HttpServletRequest req) {
        boolean reset = passwordService.completePasswordReset(request.token(), request.newPassword(),
                req.getRemoteAddr(), req.getHeader("User-Agent"));
        if (!reset) {
            return ResponseEntity.badRequest().body(StatusResponse.failed("Invalid or expired password reset token"));
        }
        return ResponseEntity
                .ok(StatusResponse.success("Password reset successfully. Please log in with your new password."));
    }
}
