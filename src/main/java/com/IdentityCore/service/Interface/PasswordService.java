package com.IdentityCore.service.Interface;

import com.IdentityCore.model.entity.User;

public interface PasswordService {

    Object initiatePasswordReset(User user, String baseUrl);

    boolean completePasswordReset(String token,
            String newPassword,
            String remoteAddr, String header);

    String hashPassword(String password);

    boolean verifyPassword(String rawPassword, String secretHash);
    
}
