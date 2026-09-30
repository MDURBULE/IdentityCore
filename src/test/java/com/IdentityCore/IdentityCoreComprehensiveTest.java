package com.IdentityCore;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.IdentityCore.dbhandler.UserRepository;
import com.IdentityCore.model.constant.UserStatus;
import com.IdentityCore.model.entity.User;
import com.IdentityCore.model.request.AuthenticatedPrincipal;
import com.IdentityCore.model.request.RegisterRequest;
import com.IdentityCore.model.request.UpdateProfileRequest;
import com.IdentityCore.model.response.JwksResponse;
import com.IdentityCore.model.response.UserResponse;
import com.IdentityCore.service.Interface.AuthService;
import com.IdentityCore.service.Interface.PasswordService;
import com.IdentityCore.service.Interface.TokenService;
import com.IdentityCore.service.Interface.UserService;

@SpringBootTest
public class IdentityCoreComprehensiveTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testCompleteIdentityFlow() {
        String randomSuffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "testuser_" + randomSuffix + "@example.com";
        String password = "Password123!";
        String firstName = "John";
        String lastName = "Doe";
        String phone = "+1234567890";

        // 1. Register User with enhanced profile data
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);
        registerRequest.setFirstName(firstName);
        registerRequest.setLastName(lastName);
        registerRequest.setPhoneNumber(phone);

        AuthService.RegisterResult regResult = authService.registerUser(registerRequest, "127.0.0.1", "JUnit-Test", "http://localhost:8080");
        assertNotNull(regResult);
        assertNotNull(regResult.publicId());
        assertEquals(email, regResult.email());
        assertEquals("PENDING_VERIFICATION", regResult.status());

        // 2. Fetch User Profile
        UUID publicId = UUID.fromString(regResult.publicId());
        Optional<User> optUser = userService.findByPublicId(publicId);
        assertTrue(optUser.isPresent());
        User user = optUser.get();
        assertEquals(firstName, user.getFirstName());
        assertEquals(lastName, user.getLastName());
        assertEquals(phone, user.getPhoneNumber());
        assertFalse(user.isEmailVerified());

        UserResponse userResponse = UserResponse.from(user);
        assertEquals(firstName, userResponse.firstName());
        assertEquals(lastName, userResponse.lastName());
        assertEquals(phone, userResponse.phoneNumber());

        // 3. Update User Profile
        UpdateProfileRequest updateReq = new UpdateProfileRequest();
        updateReq.setFirstName("Johnny");
        updateReq.setLastName("Doeman");
        updateReq.setPhoneNumber("+9876543210");
        updateReq.setAvatarUrl("https://example.com/avatar.png");

        User updatedUser = userService.updateProfile(publicId, updateReq);
        assertEquals("Johnny", updatedUser.getFirstName());
        assertEquals("Doeman", updatedUser.getLastName());
        assertEquals("+9876543210", updatedUser.getPhoneNumber());
        assertEquals("https://example.com/avatar.png", updatedUser.getAvatarUrl());

        // 4. Test Login
        AuthService.LoginResult loginResult = authService.login(email, password, "IdentityCore", "JUnit", "127.0.0.1", "JUnit-Test");
        assertNotNull(loginResult);
        assertNotNull(loginResult.accessToken());
        assertNotNull(loginResult.refreshToken());
        assertNotNull(loginResult.sessionId());

        // 5. Test Access Token Verification
        org.springframework.security.core.AuthenticatedPrincipal principal = tokenService.verifyAccessToken(loginResult.accessToken());
        assertNotNull(principal);
        assertTrue(principal instanceof AuthenticatedPrincipal);
        AuthenticatedPrincipal authPrincipal = (AuthenticatedPrincipal) principal;
        assertEquals(regResult.publicId(), authPrincipal.getUserId());
        assertEquals(loginResult.sessionId(), authPrincipal.getSessionId());

        // 6. Test Refresh Token Rotation
        AuthService.RefreshResult refreshResult = authService.refreshToken(loginResult.refreshToken(), "IdentityCore", "127.0.0.1", "JUnit-Test");
        assertNotNull(refreshResult);
        assertNotNull(refreshResult.accessToken());
        assertNotNull(refreshResult.refreshToken());

        // 7. Test JWKS response
        JwksResponse jwks = tokenService.getJwks();
        assertNotNull(jwks);
        assertFalse(jwks.keys().isEmpty());
        assertEquals("RS256", jwks.keys().get(0).alg());

        // 8. Test Password Hashing and Verification
        String hashed = passwordService.hashPassword("Secret123!");
        assertTrue(passwordService.verifyPassword("Secret123!", hashed));
        assertFalse(passwordService.verifyPassword("WrongPassword", hashed));

        System.out.println("All IdentityCore comprehensive flow tests passed successfully!");
    }
}
