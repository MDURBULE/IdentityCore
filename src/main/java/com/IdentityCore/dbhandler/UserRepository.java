package com.IdentityCore.dbhandler;

import java.sql.Connection;

import com.IdentityCore.config.Config;
import com.IdentityCore.model.constant.UserStatus;

public class UserRepository {
    
    public boolean registerUser(){
        try(Connection con = Config.getDbHandler().getConnection()) {
            
        } catch (Exception e) {
        }

        return false;
    }

    public boolean updateLastLogin(Long userId) {
        try(Connection con = Config.getDbHandler().getConnection()) {
            
        } catch (Exception e) {
        }
        return false;
    }

    public boolean updateStatusAndVerification(Long userId, UserStatus status, boolean emailVerified) {
        try(Connection con = Config.getDbHandler().getConnection()) {
            
        } catch (Exception e) {
            
        }
        return false;
    }
}
