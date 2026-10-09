package com.livescore.app.profile.services;

import com.livescore.app.profile.Profile;
import com.livescore.app.auth.enums.Role;
import com.livescore.app.league.services.LeagueAccessService;
import com.livescore.app.profile.ProfileRepository;
import com.livescore.app.profile.dto.AddRosterMemberRequest;
import com.livescore.app.profile.enums.CaptainStatus;
import com.livescore.app.profile.enums.PlayerStatus;
import com.livescore.app.team.Team;
import com.livescore.app.team.TeamRepository;
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
    private final LeagueAccessService leagueAccessService;

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
            leagueAccessService.requireLeagueRole(team.getLeague(), com.livescore.app.auth.enums.Role.LEAGUE_OWNER, com.livescore.app.auth.enums.Role.LEAGUE_ADMIN);
        }

        return profileRepository.save(profile);
    }
}