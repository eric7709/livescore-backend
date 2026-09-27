package com.zestio.app.auth.services;

import com.zestio.app.auth.InviteCode;
import com.zestio.app.auth.InviteCodeRepository;
import com.zestio.app.auth.dto.RegisterRequest;
import com.zestio.app.profile.Profile;
import com.zestio.app.profile.ProfileRepository;
import com.zestio.app.profile.enums.CaptainStatus;
import com.zestio.app.profile.enums.PlayerStatus;
import com.zestio.app.profile.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RegisterProfile {

    private static final Set<Role> LOGIN_ROLES = EnumSet.of(Role.ADMIN, Role.MODERATOR, Role.MANAGER);

    private final ProfileRepository profileRepository;
    private final InviteCodeRepository inviteCodeRepository;
    private final PasswordEncoder passwordEncoder;

    public Profile register(RegisterRequest request) {
        if (profileRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalStateException("Phone number already registered");
        }

        InviteCode invite = inviteCodeRepository.findByCode(request.getInviteCode())
                .orElseThrow(() -> new IllegalStateException("Invalid invite code"));

        if (invite.isUsed()) {
            throw new IllegalStateException("Invite code has already been used");
        }
        if (invite.getExpiryDate().isBefore(Instant.now())) {
            throw new IllegalStateException("Invite code has expired");
        }
        if (!LOGIN_ROLES.contains(invite.getRole())) {
            throw new IllegalStateException("This invite is not valid for account registration");
        }

        Profile profile = new Profile();
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setPassword(passwordEncoder.encode(request.getPassword()));
        profile.setStatus(PlayerStatus.ACTIVE);
        profile.setCaptainStatus(CaptainStatus.NONE);
        profile.setRole(invite.getRole());
        profile.setTeam(invite.getTeam());

        Profile saved = profileRepository.save(profile);

        invite.setUsed(true);
        invite.setUsedByProfileId(saved.getId());
        inviteCodeRepository.save(invite);

        return saved;
    }
}