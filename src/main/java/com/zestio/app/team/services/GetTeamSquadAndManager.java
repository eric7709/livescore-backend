package com.zestio.app.team.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.zestio.app.profile.Profile;
import com.zestio.app.profile.ProfileRepository;
import com.zestio.app.profile.enums.PlayerStatus;
import com.zestio.app.profile.enums.Position;
import com.zestio.app.profile.enums.Role;
import com.zestio.app.team.dto.Manager;
import com.zestio.app.team.dto.Player;
import com.zestio.app.team.dto.TeamSquadAndManager;
import com.zestio.app.utils.Resolver;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetTeamSquadAndManager {
    private final Resolver resolver;
    private final ProfileRepository profileRepository;

    public TeamSquadAndManager get(Long teamId, PlayerStatus status, List<Position> positions) {
        resolver.resolveTeam(teamId);
        TeamSquadAndManager teamSquadAndManager = new TeamSquadAndManager();
        List<Profile> profiles = profileRepository.findByTeamId(teamId);
        List<Profile> players = profiles.stream()
                .filter(el -> el.getRole().equals(Role.PLAYER))
                .toList();
        Profile manager = profiles.stream()
                .filter(el -> el.getRole().equals(Role.MANAGER))
                .findFirst()
                .orElse(null);
        List<Player> squad = players.stream()
                .filter(p -> status == null || p.getStatus() == status)
                .filter(p -> positions == null || positions.isEmpty() || positions.contains(p.getPosition()))
                .map(el -> {
                    return Player.toPlayer(el);
                })
                .toList();
        teamSquadAndManager.setManager(Manager.toDTO(manager));
        teamSquadAndManager.setSquad(squad);
        return teamSquadAndManager;
    }
}
