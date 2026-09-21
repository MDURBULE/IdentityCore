package com.IdentityCore.service.impl;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.IdentityCore.dbhandler.UserRepository;
import com.IdentityCore.model.constant.UserStatus;
import com.IdentityCore.model.entity.User;
import com.IdentityCore.model.objectvalues.Email;
import com.IdentityCore.model.request.RegisterRequest;
import com.IdentityCore.service.Interface.AuthService;
import com.IdentityCore.service.Interface.TokenService;
import com.IdentityCore.service.Interface.UserService;

@Service
public class AuthServiceImpl implements AuthService{

    UserRepository userRepository ;
    UserService userService;
    TokenService tokenService;

    public AuthServiceImpl(UserRepository userRepository,UserService userService,TokenService tokenService){
        this.userRepository = userRepository;
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @Override
    public RegisterResult registerUser(RegisterRequest request, String ipAddress, String userAgent,
            String baseUrl) {
        Email email = Email.of(request.getEmail());
        //add-implement paswward logic
        String haspassward = request.getPassword();

        Map<String, Object> procResult =  userRepository.registerUser(email.getRawEmail(),email.getNormalizedEmail(),haspassward,ipAddress,userAgent);  
        Long userId = (Long) procResult.get("p_user_id");
        UUID publicId = (UUID) procResult.get("p_public_id");
        String statusStr = (String) procResult.get("p_status");

        //add-implement notification
        return new RegisterResult(publicId.toString(), email.getRawEmail(), statusStr);

    }

    @Override
    public LoginResult login(String rawEmail, String rawPassword, String clientId, String deviceName, String ipAddress,
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

        TokenService.RefreshTokenIssueResult refreshIssue = tokenService.issueInitialRefreshToken(user.getId(), deviceName, ipAddress, userAgent);
        String accessToken = tokenService.generateAccessToken(user.getPublicId().toString(), clientId, refreshIssue.sessionId(), Set.of("openid", "profile", "email"));

        return new LoginResult(user.getPublicId().toString(), accessToken, refreshIssue.rawRefreshToken(), refreshIssue.sessionId(), false);
    }

    @Override
    public RefreshResult refreshToken(String incomingRefreshToken, String clientId, String ipAddress, String userAgent) {
        Optional<TokenService.RefreshTokenRotationResult> rotOpt = tokenService.rotateRefreshToken(incomingRefreshToken, ipAddress, userAgent, null, null);
        if (rotOpt.isEmpty()) {
            throw new IllegalArgumentException("INVALID_REFRESH_TOKEN");
        }

        TokenService.RefreshTokenRotationResult rot = rotOpt.get();
        User user = userRepository.findById(rot.userId()).orElseThrow(() -> new IllegalStateException("USER_NOT_FOUND"));

        String newAccessToken = tokenService.generateAccessToken(user.getPublicId().toString(), clientId, rot.sessionId(), Set.of("openid", "profile", "email"));

        return new RefreshResult(newAccessToken, rot.newRawRefreshToken(), rot.sessionId());
    }
    
}
