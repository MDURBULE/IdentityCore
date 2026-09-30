package com.IdentityCore.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.IdentityCore.model.entity.User;
import com.IdentityCore.model.request.AuthenticatedPrincipal;
import com.IdentityCore.model.request.UpdateProfileRequest;
import com.IdentityCore.model.response.RevokeAllSessionsResponse;
import com.IdentityCore.model.response.SessionResponse;
import com.IdentityCore.model.response.StatusResponse;
import com.IdentityCore.model.response.UserResponse;
import com.IdentityCore.service.Interface.TokenService;
import com.IdentityCore.service.Interface.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/users/me")
public class UserController {

    private final UserService userService;
    private final TokenService tokenService;

    public UserController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @GetMapping
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
        if (principal == null || principal.getUserId() == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userService.findByPublicId(UUID.fromString(principal.getUserId()))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PutMapping
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        if (principal == null || principal.getUserId() == null) {
            return ResponseEntity.status(401).build();
        }

        User updatedUser = userService.updateProfile(UUID.fromString(principal.getUserId()), request);
        return ResponseEntity.ok(UserResponse.from(updatedUser));
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<SessionResponse>> getSessions(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
        if (principal == null || principal.getUserId() == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userService.findByPublicId(UUID.fromString(principal.getUserId()))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<SessionResponse> sessions = tokenService.getActiveSessions(user.getId()).stream()
                .map(s -> new SessionResponse(
                        s.getId(),
                        s.getDeviceName(),
                        s.getIpAddress(),
                        s.getUserAgent(),
                        s.getExpiresAt(),
                        s.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(sessions);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<StatusResponse> revokeSession(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable Long sessionId) {
        if (principal == null || principal.getUserId() == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userService.findByPublicId(UUID.fromString(principal.getUserId()))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        tokenService.revokeSession(sessionId, user.getId());
        return ResponseEntity.ok(StatusResponse.success("Session revoked successfully"));
    }

    @PostMapping("/sessions/revoke-all")
    public ResponseEntity<RevokeAllSessionsResponse> revokeAllSessions(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            HttpServletRequest req) {
        if (principal == null || principal.getUserId() == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userService.findByPublicId(UUID.fromString(principal.getUserId()))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        int count = tokenService.revokeAllUserSessions(user.getId(), req.getRemoteAddr(), req.getHeader("User-Agent"));
        return ResponseEntity.ok(new RevokeAllSessionsResponse("SUCCESS", "All sessions revoked", count));
    }
}
