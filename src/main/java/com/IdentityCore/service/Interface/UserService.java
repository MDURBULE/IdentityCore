package com.IdentityCore.service.Interface;

import java.util.Optional;
import java.util.UUID;

import com.IdentityCore.model.entity.User;
import com.IdentityCore.model.request.UpdateProfileRequest;

public interface UserService {

    void sendEmailVerification(User user, String baseUrl);

    Optional<User> findByPublicId(UUID publicId);

    boolean verifyEmailToken(String token, String remoteAddr, String userAgent);

    Optional<User> findByEmail(String email);

    User updateProfile(UUID publicId, UpdateProfileRequest request);
}
