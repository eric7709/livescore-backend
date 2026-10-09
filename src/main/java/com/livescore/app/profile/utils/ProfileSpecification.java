package com.livescore.app.profile.utils;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.livescore.app.auth.enums.Role;
import com.livescore.app.profile.Profile;
import com.livescore.app.profile.dto.ProfileQueryParams;

import jakarta.persistence.criteria.Predicate;

public class ProfileSpecification {
        
    private static final List<Role> SEARCHABLE_ROLES = List.of(Role.PLAYER, Role.MANAGER);

    public static Specification<Profile> search(ProfileQueryParams request) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();
            
            predicates.add(root.get("role").in(SEARCHABLE_ROLES));

            // League filter (direct reference on the profile)
            if (request.getLeagueId() != null) {
                predicates.add(
                        cb.equal(root.get("league").get("id"), request.getLeagueId())
                );
            }

            // Team filter
            if (request.getTeamId() != null) {
                predicates.add(
                        cb.equal(root.get("team").get("id"), request.getTeamId())
                );
            }

            // First name
            if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("firstName")),
                                "%" + request.getFirstName().toLowerCase() + "%"
                        )
                );
            }

            // Last name
            if (request.getLastName() != null && !request.getLastName().isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("lastName")),
                                "%" + request.getLastName().toLowerCase() + "%"
                        )
                );
            }

            // Full name
            if (request.getFullName() != null && !request.getFullName().isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("fullName")),
                                "%" + request.getFullName().toLowerCase() + "%"
                        )
                );
            }

            // Phone number
            if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
                predicates.add(
                        cb.like(root.get("phoneNumber"), "%" + request.getPhoneNumber() + "%")
                );
            }

            // Squad number
            if (request.getSquadNumber() != null) {
                predicates.add(cb.equal(root.get("squadNumber"), request.getSquadNumber()));
            }

            // Player status
            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            // Profile role
            if (request.getRole() != null) {
                predicates.add(cb.equal(root.get("role"), request.getRole()));
            }

            // Position
            if (request.getPosition() != null) {
                predicates.add(cb.equal(root.get("position"), request.getPosition()));
            }

            // Captain status
            if (request.getCaptainStatus() != null) {
                predicates.add(cb.equal(root.get("captainStatus"), request.getCaptainStatus()));
            }

            // Preferred foot
            if (request.getPreferredFoot() != null) {
                predicates.add(cb.equal(root.get("preferredFoot"), request.getPreferredFoot()));
            }

            // Minimum height
            if (request.getMinHeight() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("height"), request.getMinHeight()));
            }

            // Maximum height
            if (request.getMaxHeight() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("height"), request.getMaxHeight()));
            }

            // Date of birth from
            if (request.getDateOfBirthFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dateOfBirth"), request.getDateOfBirthFrom()));
            }

            // Date of birth to
            if (request.getDateOfBirthTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dateOfBirth"), request.getDateOfBirthTo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}