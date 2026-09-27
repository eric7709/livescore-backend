package com.zestio.app.utils;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.zestio.app.competition.Competition;
import com.zestio.app.competition.CompetitionRepository;
import com.zestio.app.exceptions.NotFoundException;
import com.zestio.app.match.Match;
import com.zestio.app.match.MatchRepository;
import com.zestio.app.matchLineup.MatchLineup;
import com.zestio.app.matchLineup.MatchLineupRepository;
import com.zestio.app.profile.Profile;
import com.zestio.app.profile.ProfileRepository;
import com.zestio.app.team.Team;
import com.zestio.app.team.TeamRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class Resolver {

    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final ProfileRepository profileRepository;
    private final CompetitionRepository competitionRepository;
    private final MatchLineupRepository matchLineupRepository;

    public Match resolveMatch(Long id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Match not found"));
    }

    public MatchLineup resolveMatchLineup(Long id) {
        return matchLineupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Match Lineup not found"));
    }

    public Set<Team> resolveTeams(Set<Long> teamIds) {
        if (teamIds == null || teamIds.isEmpty()) {
            return Set.of();
        }
        Set<Long> uniqueIds = new HashSet<>(teamIds);
        var teams = teamRepository.findAllById(uniqueIds);
        if (teams.size() != uniqueIds.size()) {
            throw new NotFoundException("One or more teamIds were not found");
        }
        return new HashSet<>(teams);
    }

    public Team resolveTeam(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Team with the id " + id + " not found"));
    }

    public Competition resolveCompetition(Long id) {
        return competitionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Competition with the id " + id + " not found"));
    }

    public Profile resolveProfile(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Profile not found"));
    }

    public boolean isHome(Match match, Long teamId) {
        return match.getHomeTeam().getId().equals(teamId);
    }

    public String fullName(Profile profile) {
        return profile.getFirstName() + " " + profile.getLastName();
    }
}