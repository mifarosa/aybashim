package com.aybashim.backend.config;

import com.aybashim.backend.model.AppUser;
import com.aybashim.backend.repository.UserRepository;
import com.aybashim.backend.service.CurrentUser;
import com.aybashim.backend.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;

    public AuthInterceptor(JwtService jwtService, UserRepository userRepository, CurrentUser currentUser) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing bearer token");
            return false;
        }

        Long userId = jwtService.validateAndGetUserId(authorization.substring(7));
        if (userId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return false;
        }

        AppUser user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
            return false;
        }

        currentUser.set(user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        currentUser.clear();
    }
}
