package com.IdentityCore.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.IdentityCore.model.response.UserResponse;

@RestController
@RequestMapping("/v1/users/me")
public class UserController {
    

    @GetMapping
    public ResponseEntity<UserResponse>  getProfile(String session){
        return ResponseEntity.ok(null);
    }
}
