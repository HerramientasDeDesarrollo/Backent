package com.example.guiavirtual.Config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    @Value("${api.key}")
    private String apiKey;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String providedApiKey = request.getHeader("X-API-KEY");
        
        if (providedApiKey == null || !providedApiKey.equals(apiKey)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        
        return true;
    }
}