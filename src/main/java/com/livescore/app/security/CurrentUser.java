package com.livescore.app.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.livescore.app.auth.User;

@Component
public class CurrentUser {

    /**
     * Returns the currently authenticated User.
     */
    public User get() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Not authenticated"
            );
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof User user)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user could not be resolved"
            );
        }

        return user;
    }

    /**
     * Returns the current user's ID.
     */
    public Long id() {
        return get().getId();
    }

    /**
     * Returns the current user's email.
     */
    public String email() {
        return get().getEmail();
    }

    /**
     * Returns the current user's role.
     */
    public String role() {
        return get().getRole() != null
                ? get().getRole().name()
                : null;
    }
}