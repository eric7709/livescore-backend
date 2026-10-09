package com.livescore.app.utils;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.livescore.app.auth.tempUserAndPasswordToken.TempUser;
import com.livescore.app.auth.tempUserAndPasswordToken.TempUserRepository;
import com.livescore.app.auth.User;
import com.livescore.app.auth.UserRepository;
import com.livescore.app.competition.Competition;
import com.livescore.app.competition.CompetitionRepository;
import com.livescore.app.exceptions.NotFoundException;
import com.livescore.app.league.League;
import com.livescore.app.league.LeagueRepository;
import com.livescore.app.match.Match;
import com.livescore.app.match.MatchRepository;
import com.livescore.app.matchLineup.MatchLineup;
import com.livescore.app.matchLineup.MatchLineupRepository;
import com.livescore.app.profile.Profile;
import com.livescore.app.profile.ProfileRepository;
import com.livescore.app.team.Team;
import com.livescore.app.team.TeamRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class Resolver {

    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final LeagueRepository leagueRepository;
    private final UserRepository userRepository;
    private final TempUserRepository tempUserRepository;
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
    public User resolveUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with the id " + id + " not found"));
    }
    
    public TempUser resolveTempUser(Long id) {
        return tempUserRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with the id " + id + " not found"));
    }
    
    public League resolveLeague(Long id) {
        return leagueRepository.findById(id)
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