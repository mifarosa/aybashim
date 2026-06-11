package com.aybashim.backend.service;

import com.aybashim.backend.model.AppUser;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    private final ThreadLocal<AppUser> current = new ThreadLocal<>();

    public void set(AppUser user) {
        current.set(user);
    }

    public AppUser get() {
        AppUser user = current.get();
        if (user == null) {
            throw new IllegalStateException("Authenticated user not found");
        }
        return user;
    }

    public void clear() {
        current.remove();
    }
}
