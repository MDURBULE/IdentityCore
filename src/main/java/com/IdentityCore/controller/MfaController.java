package com.IdentityCore.controller;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.IdentityCore.config.Config;
import com.IdentityCore.model.request.AuthenticatedPrincipal;
import com.IdentityCore.model.request.MfaVerifyRequest;
import com.IdentityCore.model.response.MfaEnrollResponse;
import com.IdentityCore.model.response.StatusResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/mfa")
public class MfaController {

    private final SecureRandom secureRandom = new SecureRandom();

    @PostMapping("/enroll")
    public ResponseEntity<MfaEnrollResponse> enroll(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
        if (principal == null || principal.getUserId() == null) {
            return ResponseEntity.status(401).build();
        }

        byte[] secretBytes = new byte[20];
        secureRandom.nextBytes(secretBytes);
        String secret = Base64.getUrlEncoder().withoutPadding().encodeToString(secretBytes);

        String appName = Config.getCpx() != null ? Config.getCpx().getAppName() : "IdentityCore";
        String qrCodeUri = "otpauth://totp/" + appName + ":" + principal.getUserId() + "?secret=" + secret + "&issuer=" + appName;

        Config.setRedisKeyWithTtl("mfa_enroll:" + principal.getUserId(), secret, 10 * 60);

        return ResponseEntity.ok(new MfaEnrollResponse(
                secret,
                qrCodeUri,
                "Scan the QR code with your authenticator app and verify with a code."
        ));
    }

    @PostMapping("/verify")
    public ResponseEntity<StatusResponse> verify(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody MfaVerifyRequest request) {
        if (principal == null || principal.getUserId() == null) {
            return ResponseEntity.status(401).build();
        }

        String pendingSecret = Config.getKeyFromRedis("mfa_enroll:" + principal.getUserId(), false);
        if (pendingSecret == null) {
            return ResponseEntity.badRequest().body(StatusResponse.failed("No pending MFA enrollment found"));
        }

        if (request.totpCode() != null && request.totpCode().length() == 6) {
            Config.setRedisKey("mfa_secret:" + principal.getUserId(), pendingSecret);
            Config.deleteRedisKey("mfa_enroll:" + principal.getUserId());
            return ResponseEntity.ok(StatusResponse.success("MFA verified and enabled successfully"));
        }

        return ResponseEntity.badRequest().body(StatusResponse.failed("Invalid TOTP verification code"));
    }
}
