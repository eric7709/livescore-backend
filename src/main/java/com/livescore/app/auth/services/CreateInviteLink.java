package com.livescore.app.auth.services;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;
import java.util.Set;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.auth.tempUserAndPasswordToken.TempUser;
import com.livescore.app.auth.tempUserAndPasswordToken.TempUserRepository;
import com.livescore.app.auth.UserRepository;
import com.livescore.app.auth.dto.TempUserDTO;
import com.livescore.app.auth.enums.Role;
import com.livescore.app.auth.events.TempUserCreatedEvent;
import com.livescore.app.exceptions.BadRequestException;
import com.livescore.app.league.League;
import com.livescore.app.team.Team;
import com.livescore.app.utils.Resolver;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateInviteLink {
    private static final SecureRandom RANDOM = new SecureRandom();
    /** Roles that can be granted through an invite link. Adjust to your rules. */
    private static final Set<Role> INVITABLE_ROLES =
        Set.of(Role.MANAGER, Role.STAFF, Role.LEAGUE_ADMIN, Role.MODERATOR, Role.ADMIN);
    /** Roles that belong to no league: no league lookup, no team. */
    private static final Set<Role> LEAGUELESS_ROLES = Set.of(Role.ADMIN);

    private final Resolver resolver;
    private final TempUserRepository tempUserRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public TempUser execute(TempUserDTO dto) {
        String email = dto.getEmail().trim().toLowerCase(Locale.ROOT);
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BadRequestException("A user already exists with this email");
        }

        Role role = dto.getRole();
        if (role == null || !INVITABLE_ROLES.contains(role)) {
            throw new BadRequestException("This role cannot be invited");
        }

        boolean leagueless = LEAGUELESS_ROLES.contains(role);

        League league = null;

        Team team = null;

        if (!leagueless) {
            if (dto.getLeagueId() == null) {
                throw new BadRequestException("A league is required for this role");
            }
            league = resolver.resolveLeague(dto.getLeagueId());

            if (dto.getTeamId() != null) {
                team = resolver.resolveTeam(dto.getTeamId());
                if (team.getLeague() == null || !team.getLeague().getId().equals(league.getId())) {
                    throw new BadRequestException("Team does not belong to this league");
                }
            } else if (role == Role.MANAGER) {
                throw new BadRequestException("A team is required when inviting a manager");
            }
        }

        // Replace any earlier pending invite for this email (after validation,
        // so a failed request doesn't wipe a valid existing invite).
        tempUserRepository.findByEmail(email).ifPresent(existing -> {
            tempUserRepository.delete(existing);
            tempUserRepository.flush(); // delete must hit the DB before the insert below
        });

        TempUser tempUser = new TempUser();
        tempUser.setFirstName(dto.getFirstName());
        tempUser.setLastName(dto.getLastName());
        tempUser.setEmail(email);
        tempUser.setLeague(league);
        tempUser.setTeam(team);
        tempUser.setRole(role);
        tempUser.setToken(generateToken());

        TempUser saved = tempUserRepository.save(tempUser);
        eventPublisher.publishEvent(
                new TempUserCreatedEvent(saved.getEmail(), saved.getFirstName(), saved.getToken(), saved.getRole()));
        return saved;
    }

    private static String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}