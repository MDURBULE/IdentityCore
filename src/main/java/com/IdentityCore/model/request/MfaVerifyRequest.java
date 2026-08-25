package com.IdentityCore.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MfaVerifyRequest(
    @NotBlank(message = "TOTP code is required")
    @Size(min = 6, max = 6, message = "TOTP code must be 6 digits")
    String totpCode
) {}
