package com.zestio.app.profile.services;

import com.zestio.app.profile.Profile;
import com.zestio.app.profile.ProfileRepository;
import com.zestio.app.profile.dto.AddRosterMemberRequest;
import com.zestio.app.profile.enums.CaptainStatus;
import com.zestio.app.profile.enums.PlayerStatus;
import com.zestio.app.profile.enums.Role;
import com.zestio.app.team.Team;
import com.zestio.app.team.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AddRosterMember {

    private static final Set<Role> ROSTER_ROLES = EnumSet.of(Role.PLAYER, Role.STAFF);

    private final ProfileRepository profileRepository;
    private final TeamRepository teamRepository;

    public Profile add(AddRosterMemberRequest request) {
        if (!ROSTER_ROLES.contains(request.getRole())) {
            throw new IllegalStateException("Only PLAYER or STAFF can be added directly to a roster");
        }
        if (profileRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalStateException("Phone number already registered");
        }

        Profile profile = new Profile();
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setPassword(null); // no login access
        profile.setStatus(PlayerStatus.ACTIVE);
        profile.setCaptainStatus(CaptainStatus.NONE);
        profile.setRole(request.getRole());
        profile.setPosition(request.getPosition());
        profile.setSquadNumber(request.getSquadNumber());

        profile.setPreferredFoot(request.getPreferredFoot());
        profile.setHeight(request.getHeight());
        profile.setDateOfBirth(request.getDateOfBirth());

        if (request.getTeamId() != null) {
            Team team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new IllegalStateException("Team not found"));
            profile.setTeam(team);
        }

        return profileRepository.save(profile);
    }
}