package com.IdentityCore.model.request;

import java.time.Instant;
import java.util.Set;

public class AuthenticatedPrincipal {
    private String userId;
    private String clientId;
    private String authenticationMethod;
    private Instant authenticationTime;
    private String sessionId;
    private Set<String> scopes;
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getClientId() {
        return clientId;
    }
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    public String getAuthenticationMethod() {
        return authenticationMethod;
    }
    public void setAuthenticationMethod(String authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }
    public Instant getAuthenticationTime() {
        return authenticationTime;
    }
    public void setAuthenticationTime(Instant authenticationTime) {
        this.authenticationTime = authenticationTime;
    }
    public String getSessionId() {
        return sessionId;
    }
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    public Set<String> getScopes() {
        return scopes;
    }
    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }

    
}
