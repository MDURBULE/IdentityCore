package com.IdentityCore.service.impl;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.IdentityCore.dbhandler.UserRepository;
import com.IdentityCore.model.constant.UserStatus;
import com.IdentityCore.model.entity.User;
import com.IdentityCore.model.objectvalues.Email;
import com.IdentityCore.model.response.AuthResponse;
import com.IdentityCore.service.Interface.AuthService;
import com.IdentityCore.service.Interface.UserService;

@Service
public class AuthServiceImpl implements AuthService{

    UserRepository userRepository ;
    UserService userService;

    public AuthServiceImpl(UserRepository userRepository,UserService userService){
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Override
    public RegisterResult registerUser(String rawEmail, String rawPassword, String ipAddress, String userAgent,
            String baseUrl) {
        Email email = Email.of(rawEmail);
        //add-implement paswward logic
        String haspassward = rawPassword;

        Map<String, Object> procResult =  userRepository.registerUser(email.getRawEmail(),email.getNormalizedEmail(),haspassward,ipAddress,userAgent);  
        Long userId = (Long) procResult.get("p_user_id");
        UUID publicId = (UUID) procResult.get("p_public_id");
        String statusStr = (String) procResult.get("p_status");

        //add-implement notification
        return new RegisterResult(publicId.toString(), email.getRawEmail(), statusStr);

    }

    @Override
    public AuthResult login(String rawEmail, String rawPassword, String clientId, String deviceName, String ipAddress,
            String userAgent) {
        Email email = Email.of(rawEmail);
        Optional<User> optUser = userRepository.findByNormalizedEmail(email.getNormalizedEmail());
        if(optUser.isEmpty()){
            throw new IllegalArgumentException("INVALID_CREDENTIALS");
        }

        User user = optUser.get();
        if(user.getStatus()!=UserStatus.ACTIVE && user.getStatus()!=UserStatus.PENDING_VERIFICATION){
            throw new IllegalArgumentException("ACCOUNT_INACTIVE");
        }

        //add-implement passward / credencial check
        //add-implement mfa check

        return new AuthResult();
    }

    @Override
    public AuthResult refreshToken(String incomingRefreshToken, String clientId, String ipAddress, String userAgent) {
        

        return new AuthResult();
    }
    
}
