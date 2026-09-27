package com.zestio.app.auth.services;

import com.zestio.app.auth.InviteCode;
import com.zestio.app.auth.InviteCodeRepository;
import com.zestio.app.auth.dto.CreateInviteRequest;
import com.zestio.app.profile.Profile;
import com.zestio.app.profile.ProfileRepository;
import com.zestio.app.profile.enums.Role;
import com.zestio.app.team.Team;
import com.zestio.app.team.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GenerateInviteCode {

    private static final Set<Role> LOGIN_ROLES = EnumSet.of(Role.ADMIN, Role.MODERATOR, Role.MANAGER);

    private final InviteCodeRepository inviteCodeRepository;
    private final ProfileRepository profileRepository;
    private final TeamRepository teamRepository;

    public InviteCode generate(CreateInviteRequest request) {
        if (!LOGIN_ROLES.contains(request.getRole())) {
            throw new IllegalStateException(
                    "Invites are only for accounts that log in (ADMIN, MODERATOR, MANAGER). " +
                    "Add players and staff directly to a team's roster instead."
            );
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Profile creator = profileRepository.findByPhoneNumber(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated profile not found"));

        InviteCode invite = new InviteCode();
        invite.setCode(generateCode());
        invite.setRole(request.getRole());
        invite.setCreatedBy(creator);
        invite.setExpiryDate(Instant.now().plus(
                request.getExpiresInDays() != null ? request.getExpiresInDays() : 7,
                ChronoUnit.DAYS
        ));
        invite.setUsed(false);

        if (request.getTeamId() != null) {
            Team team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new IllegalStateException("Team not found"));
            invite.setTeam(team);
        }

        return inviteCodeRepository.save(invite);
    }

    private String generateCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}