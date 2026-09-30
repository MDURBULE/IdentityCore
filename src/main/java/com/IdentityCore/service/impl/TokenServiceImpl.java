package com.IdentityCore.service.impl;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;

import com.IdentityCore.config.Config;
import com.IdentityCore.dbhandler.SecurityEventRepository;
import com.IdentityCore.model.request.RefreshToken;
import com.IdentityCore.model.response.JwksResponse;
import com.IdentityCore.model.response.JwksResponse.JwkKeyDto;
import com.IdentityCore.service.Interface.TokenService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TokenServiceImpl implements TokenService {

    private static final String KEY_ID = "identitycore-rsa-key-1";
    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;
    private final SecureRandom secureRandom = new SecureRandom();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SecurityEventRepository securityEventRepository;

    public TokenServiceImpl(SecurityEventRepository securityEventRepository) {
        this.securityEventRepository = securityEventRepository;
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();
            this.publicKey = (RSAPublicKey) kp.getPublic();
            this.privateKey = (RSAPrivateKey) kp.getPrivate();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to initialize RSA key pair for JWT signing", e);
        }
    }

    @Override
    public AuthenticatedPrincipal verifyAccessToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT format");
            }

            String signingInput = parts[0] + "." + parts[1];
            byte[] signatureBytes = Base64.getUrlDecoder().decode(parts[2]);

            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(publicKey);
            verifier.update(signingInput.getBytes(StandardCharsets.UTF_8));
            if (!verifier.verify(signatureBytes)) {
                throw new IllegalArgumentException("Invalid JWT signature");
            }

            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            Map<String, Object> claims = objectMapper.readValue(payloadBytes, new TypeReference<Map<String, Object>>() {});

            Number exp = (Number) claims.get("exp");
            if (exp != null && Instant.ofEpochSecond(exp.longValue()).isBefore(Instant.now())) {
                throw new IllegalArgumentException("JWT token has expired");
            }

            String sub = (String) claims.get("sub");
            String aud = (String) claims.get("aud");
            String sessionId = (String) claims.get("sid");

            Set<String> scopes = new HashSet<>();
            Object scopeObj = claims.get("scope");
            if (scopeObj instanceof String s) {
                scopes.addAll(Arrays.asList(s.split(" ")));
            } else if (scopeObj instanceof List<?> list) {
                for (Object item : list) {
                    scopes.add(String.valueOf(item));
                }
            }

            return new com.IdentityCore.model.request.AuthenticatedPrincipal(sub, aud, sessionId, scopes);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to verify access token: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateAccessToken(String publicUserId, String clientId, String sessionId, Set<String> scopes) {
        try {
            long now = Instant.now().getEpochSecond();
            long expSeconds = Config.getCpx() != null ? Config.getCpx().getJwtExpirationSeconds() : 900;
            String issuer = Config.getCpx() != null ? Config.getCpx().getJwtIssuer() : "https://identity.swiunflow.com";

            Map<String, Object> header = Map.of(
                    "alg", "RS256",
                    "typ", "JWT",
                    "kid", KEY_ID
            );

            Map<String, Object> payload = Map.of(
                    "iss", issuer,
                    "sub", publicUserId,
                    "aud", clientId != null ? clientId : "IdentityCore",
                    "sid", sessionId != null ? sessionId : "",
                    "scope", scopes != null ? String.join(" ", scopes) : "",
                    "iat", now,
                    "exp", now + expSeconds
            );

            String encodedHeader = Base64.getUrlEncoder().withoutPadding().encodeToString(objectMapper.writeValueAsBytes(header));
            String encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(objectMapper.writeValueAsBytes(payload));
            String signingInput = encodedHeader + "." + encodedPayload;

            Signature signer = Signature.getInstance("SHA256withRSA");
            signer.initSign(privateKey);
            signer.update(signingInput.getBytes(StandardCharsets.UTF_8));
            byte[] signature = signer.sign();
            String encodedSignature = Base64.getUrlEncoder().withoutPadding().encodeToString(signature);

            return signingInput + "." + encodedSignature;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate access token", e);
        }
    }

    @Override
    public String generateRawRefreshToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Override
    public String hashRefreshToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    @Override
    public RefreshTokenIssueResult issueInitialRefreshToken(Long userId, String deviceName, String ipAddress,
            String userAgent) {
        String rawToken = generateRawRefreshToken();
        String tokenHash = hashRefreshToken(rawToken);
        UUID familyId = UUID.randomUUID();
        Instant expiresAt = Instant.now().plus(30, ChronoUnit.DAYS);

        String sql = "SELECT * FROM sp_issue_refresh_token_v1(?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Config.getDbHandler().getConnection();
             PreparedStatement pt = con.prepareStatement(sql)) {
            pt.setLong(1, userId);
            pt.setString(2, tokenHash);
            pt.setObject(3, familyId);
            pt.setString(4, deviceName != null ? deviceName : "Default Device");
            pt.setString(5, ipAddress);
            pt.setString(6, userAgent);
            pt.setTimestamp(7, Timestamp.from(expiresAt));

            try (ResultSet rs = pt.executeQuery()) {
                if (rs.next()) {
                    long tokenId = rs.getLong("p_token_id");
                    return new RefreshTokenIssueResult(rawToken, String.valueOf(tokenId));
                }
            }
        } catch (Exception e) {
            Config.getLgr().error("Failed to issue initial refresh token for userId: {}", userId, e);
            throw new RuntimeException("Failed to issue refresh token", e);
        }
        throw new RuntimeException("Failed to issue refresh token");
    }

    @Override
    public Optional<RefreshTokenRotationResult> rotateRefreshToken(String incomingRawToken, String ipAddress,
            String userAgent, String userEmail, String userName) {
        if (incomingRawToken == null || incomingRawToken.isBlank()) {
            return Optional.empty();
        }

        String oldTokenHash = hashRefreshToken(incomingRawToken);
        String newRawToken = generateRawRefreshToken();
        String newTokenHash = hashRefreshToken(newRawToken);
        Instant newExpiresAt = Instant.now().plus(30, ChronoUnit.DAYS);

        String sql = "SELECT * FROM sp_rotate_refresh_token_v1(?, ?, ?, ?, ?)";
        try (Connection con = Config.getDbHandler().getConnection();
             PreparedStatement pt = con.prepareStatement(sql)) {
            pt.setString(1, oldTokenHash);
            pt.setString(2, newTokenHash);
            pt.setTimestamp(3, Timestamp.from(newExpiresAt));
            pt.setString(4, ipAddress);
            pt.setString(5, userAgent);

            try (ResultSet rs = pt.executeQuery()) {
                if (rs.next()) {
                    boolean success = rs.getBoolean("p_success");
                    if (success) {
                        long userId = rs.getLong("p_user_id");
                        long newTokenId = rs.getLong("p_new_token_id");
                        return Optional.of(new RefreshTokenRotationResult(userId, String.valueOf(newTokenId), newRawToken));
                    } else {
                        String errorCode = rs.getString("p_error_code");
                        Config.getLgr().warn("Refresh token rotation failed with code: {}", errorCode);
                        if ("TOKEN_REUSE_DETECTED".equals(errorCode)) {
                            securityEventRepository.recordSecurityEvent(null, "REFRESH_TOKEN_REUSE_DETECTED", ipAddress, userAgent, null,
                                    "{\"error\":\"TOKEN_REUSE_DETECTED\"}");
                        }
                    }
                }
            }
        } catch (Exception e) {
            Config.getLgr().error("Failed to rotate refresh token", e);
        }
        return Optional.empty();
    }

    @Override
    public List<RefreshToken> getActiveSessions(Long userId) {
        List<RefreshToken> sessions = new ArrayList<>();
        String sql = "SELECT * FROM refresh_tokens WHERE user_id = ? AND revoked_at IS NULL AND expires_at > CURRENT_TIMESTAMP ORDER BY created_at DESC";
        try (Connection con = Config.getDbHandler().getConnection();
             PreparedStatement pt = con.prepareStatement(sql)) {
            pt.setLong(1, userId);
            try (ResultSet rs = pt.executeQuery()) {
                while (rs.next()) {
                    RefreshToken token = new RefreshToken();
                    token.setId(rs.getLong("id"));
                    token.setUserId(rs.getLong("user_id"));
                    token.setTokenHash(rs.getString("token_hash"));
                    token.setFamilyId((UUID) rs.getObject("family_id"));
                    token.setDeviceName(rs.getString("device_name"));
                    token.setIpAddress(rs.getString("ip_address"));
                    token.setUserAgent(rs.getString("user_agent"));
                    if (rs.getTimestamp("expires_at") != null) token.setExpiresAt(rs.getTimestamp("expires_at").toInstant());
                    if (rs.getTimestamp("used_at") != null) token.setUsedAt(rs.getTimestamp("used_at").toInstant());
                    if (rs.getTimestamp("revoked_at") != null) token.setRevokedAt(rs.getTimestamp("revoked_at").toInstant());
                    if (rs.getTimestamp("created_at") != null) token.setCreatedAt(rs.getTimestamp("created_at").toInstant());
                    sessions.add(token);
                }
            }
        } catch (Exception e) {
            Config.getLgr().error("Failed to fetch active sessions for userId: {}", userId, e);
        }
        return sessions;
    }

    @Override
    public void revokeSession(Long sessionId, Long userId) {
        String sql = "SELECT sp_revoke_session_v1(?, ?)";
        try (Connection con = Config.getDbHandler().getConnection();
             PreparedStatement pt = con.prepareStatement(sql)) {
            pt.setLong(1, sessionId);
            pt.setLong(2, userId);
            pt.execute();
        } catch (Exception e) {
            Config.getLgr().error("Failed to revoke session: {} for userId: {}", sessionId, userId, e);
        }
    }

    @Override
    public int revokeAllUserSessions(Long userId, String ipAddress, String userAgent) {
        String sql = "SELECT * FROM sp_revoke_all_user_sessions_v1(?)";
        int count = 0;
        try (Connection con = Config.getDbHandler().getConnection();
             PreparedStatement pt = con.prepareStatement(sql)) {
            pt.setLong(1, userId);
            try (ResultSet rs = pt.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt("p_revoked_count");
                }
            }
            securityEventRepository.recordSecurityEvent(userId, "ALL_SESSIONS_REVOKED", ipAddress, userAgent, null,
                    "{\"revokedCount\":" + count + "}");
        } catch (Exception e) {
            Config.getLgr().error("Failed to revoke all user sessions for userId: {}", userId, e);
        }
        return count;
    }

    @Override
    public JwksResponse getJwks() {
        String n = toBase64Url(publicKey.getModulus());
        String e = toBase64Url(publicKey.getPublicExponent());
        JwkKeyDto key = new JwkKeyDto("RSA", "sig", "RS256", KEY_ID, n, e);
        return new JwksResponse(Collections.singletonList(key));
    }

    private static String toBase64Url(BigInteger bigInt) {
        byte[] bytes = bigInt.toByteArray();
        if (bytes.length > 0 && bytes[0] == 0) {
            bytes = Arrays.copyOfRange(bytes, 1, bytes.length);
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
