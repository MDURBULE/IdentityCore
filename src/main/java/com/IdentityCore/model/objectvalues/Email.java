package com.IdentityCore.model.objectvalues;

import java.util.Objects;
import java.util.regex.Pattern;

public final class Email {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private final String rawEmail;
    private final String normalizedEmail;

    public Email(String rawEmail) {
        if (rawEmail == null || rawEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Email address cannot be empty.");
        }
        String trimmed = rawEmail.trim();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + rawEmail);
        }
        this.rawEmail = trimmed;
        this.normalizedEmail = trimmed.toLowerCase();
    }

    public static Email of(String rawEmail) {
        return new Email(rawEmail);
    }

    public String getRawEmail() {
        return rawEmail;
    }

    public String getNormalizedEmail() {
        return normalizedEmail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(normalizedEmail, email.normalizedEmail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(normalizedEmail);
    }

    @Override
    public String toString() {
        return rawEmail;
    }
}
