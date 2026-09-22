package com.IdentityCore.utils;

import jakarta.servlet.http.HttpServletRequest;

public class Utilities {
    
    public static String getBaseUrl(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
    }
}
