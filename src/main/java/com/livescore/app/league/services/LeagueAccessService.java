package com.livescore.app.league.services;

import java.util.Arrays;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.livescore.app.auth.enums.Role;
import com.livescore.app.exceptions.BadRequestException;
import com.livescore.app.league.League;
import com.livescore.app.profile.Profile;
import com.livescore.app.profile.ProfileRepository;

import lombok.RequiredArgsConstructor;

/**
 * Central place for "is the currently authenticated profile allowed to do
 * this?" checks that are scoped to a specific league, on top of whatever
 * coarse-grained {@code @PreAuthorize} checks already gate the controller
 * endpoints.
 *
 * <p>A global {@link Role#ADMIN} always passes every check here. Below that,
 * a profile belongs to exactly one league (via {@link Profile#getLeague()}),
 * and its {@link Profile#getRole()} decides what it may do inside it.
 */
@Service
@RequiredArgsConstructor
public class LeagueAccessService {

    private final ProfileRepository profileRepository;

    /**
     * Requires that the current profile belongs to the given league (any
     * role), or is a global ADMIN.
     */
    public void requireLeague(League league) {
        if (league == null) {
            throw new BadRequestException("League is required");
        }

        Profile current = currentProfile();

        if (current.getRole() == Role.ADMIN) {
            return;
        }

        if (!belongsToLeague(current, league)) {
            throw new AccessDeniedException(
                    "You do not have access to league " + league.getId());
        }
    }

    /**
     * Requires that the current profile belongs to the given league AND holds
     * one of the given roles, or is a global ADMIN.
     */
    public void requireLeagueRole(League league, Role... roles) {
        if (league == null) {
            throw new BadRequestException("League is required");
        }

        Profile current = currentProfile();

        if (current.getRole() == Role.ADMIN) {
            return;
        }

        boolean hasRole = belongsToLeague(current, league)
                && Arrays.asList(roles).contains(current.getRole());

        if (!hasRole) {
            throw new AccessDeniedException(
                    "You do not have the required role in league " + league.getId());
        }
    }

    /**
     * Requires that the current profile's own (global) role is one of the
     * given roles, with no league scoping. Used for actions gated purely by
     * "what kind of profile are you", e.g. creating a new profile at all.
     */
    public void requireAnyRole(Role... roles) {
        Profile current = currentProfile();

        for (Role role : roles) {
            if (current.getRole() == role) {
                return;
            }
        }

        throw new AccessDeniedException("You do not have permission to perform this action");
    }

    private boolean belongsToLeague(Profile profile, League league) {
        return profile.getLeague() != null
                && profile.getLeague().getId().equals(league.getId());
    }

    private Profile currentProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            throw new AccessDeniedException("Authentication is required");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Profile profile) {
            return profile;
        }

        // Fall back to loading the current profile by its authenticated
        // username (phone number), for setups where the principal is a
        // Spring Security UserDetails wrapper rather than the Profile itself.
        String phoneNumber = authentication.getName();
        return profileRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new AccessDeniedException("Authenticated profile could not be resolved"));
    }
}