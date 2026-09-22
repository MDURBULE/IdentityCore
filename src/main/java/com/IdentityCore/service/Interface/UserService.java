package com.IdentityCore.service.Interface;

import java.util.Optional;
import java.util.UUID;

import com.IdentityCore.model.entity.User;

public interface UserService {

    void sendEmailVerification(User user, String baseUrl);

    Optional<User> findByPublicId(UUID fromString);

    boolean verifyEmailToken(String token, String remoteAddr, String header);

    Optional<User> findByEmail(String email);
    
}
