package com.IdentityCore.service.impl;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.IdentityCore.model.entity.User;
import com.IdentityCore.service.Interface.UserService;

@Service
public class UserServiceImpl implements UserService{

    @Override
    public void sendEmailVerification(User user, String baseUrl) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendEmailVerification'");
    }

    @Override
    public Optional<User> findByPublicId(UUID fromString){
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findByPublicId'");
    }

    @Override
    public boolean verifyEmailToken(String token, String remoteAddr, String header) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'verifyEmailToken'");
    }

    @Override
    public Optional<User> findByEmail(String email) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findByEmail'");
    }

}
