package com.zestio.app.profile.helpers;

import org.springframework.data.jpa.domain.Specification;

import com.zestio.app.profile.Profile;
import com.zestio.app.profile.enums.Position;
import com.zestio.app.profile.enums.Role;

import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

public class ProfileSpecification {

        public static Specification<Profile> filterBy(
                        String search,
                        Role role,
                        Position position,
                        Integer squadNumber,
                        Long teamId) {

                return (root, query, criteriaBuilder) -> {

                        List<Predicate> predicates = new ArrayList<>();

                        // Search firstName OR lastName OR phoneNumber
                        if (search != null && !search.trim().isEmpty()) {

                                String pattern = "%" + search.trim().toLowerCase() + "%";

                                Predicate fullName = criteriaBuilder.like(
                                                criteriaBuilder.lower(root.get("fullName")),
                                                pattern);

                                Predicate phoneNumber = criteriaBuilder.like(
                                                criteriaBuilder.lower(root.get("phoneNumber")),
                                                pattern);
                                predicates.add(
                                                criteriaBuilder.or(
                                                                fullName,
                                                                phoneNumber));
                        }

                        // Role
                        if (role != null) {
                                predicates.add(
                                                criteriaBuilder.equal(root.get("role"), role));
                        }

                        // Position
                        if (position != null) {
                                predicates.add(
                                                criteriaBuilder.equal(root.get("position"), position));
                        }

                        // Squad Number
                        if (squadNumber != null) {
                                predicates.add(
                                                criteriaBuilder.equal(root.get("squadNumber"), squadNumber));
                        }

                        // Team ID
                        if (teamId != null) {
                                predicates.add(
                                                criteriaBuilder.equal(
                                                                root.get("team").get("id"),
                                                                teamId));
                        }

                        return criteriaBuilder.and(
                                        predicates.toArray(new Predicate[0]));
                };
        }
}