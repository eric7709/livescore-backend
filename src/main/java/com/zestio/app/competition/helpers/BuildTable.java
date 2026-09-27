package com.zestio.app.competition.helpers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.zestio.app.competition.Competition;
import com.zestio.app.competition.dto.TeamStandingDTO;
import com.zestio.app.match.Match;
import com.zestio.app.match.enums.MatchStatus;
import com.zestio.app.team.Team;
import com.zestio.app.team.TeamRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BuildTable {

    private final TeamRepository teamRepository;

    public List<TeamStandingDTO> buildTable(
            Competition competition,
            List<Match> matches,
            boolean includesLiveMatches) {

        Map<Long, TeamStandingDTO> standingsByTeam = new LinkedHashMap<>();
        Map<Long, List<Character>> resultsByTeam = new LinkedHashMap<>();

        // 1. Seed EVERY team that must appear, including 0-match teams
        for (Team team : resolveTeams(competition, matches)) {
            ensureStanding(team, standingsByTeam, resultsByTeam);
        }

        // 2. Process match results ONLY for finished or active live matches
        if (matches != null) {
            for (Match match : matches) {
                Team homeTeam = match.getHomeTeam();
                Team awayTeam = match.getAwayTeam();

                if (homeTeam == null || awayTeam == null) {
                    continue;
                }

                boolean isFinished = match.getStatus() == MatchStatus.FINISHED;
                boolean isLive = match.getStatus() == MatchStatus.LIVE;

                if (isFinished || (includesLiveMatches && isLive)) {
                    TeamStandingDTO homeStanding = ensureStanding(homeTeam, standingsByTeam, resultsByTeam);
                    TeamStandingDTO awayStanding = ensureStanding(awayTeam, standingsByTeam, resultsByTeam);

                    int homeScore = scoreOrZero(match.getHomeScore());
                    int awayScore = scoreOrZero(match.getAwayScore());

                    applyFixtureTotals(homeStanding, awayStanding, homeScore, awayScore);

                    // Both finished and live (when enabled) record match outcomes to form history
                    applyFixtureResult(
                            homeStanding,
                            awayStanding,
                            resultsByTeam.get(homeTeam.getId()),
                            resultsByTeam.get(awayTeam.getId()),
                            homeScore,
                            awayScore);
                }
            }
        }

        // 3. Finalize Goal Difference & Form
        standingsByTeam.values().forEach(standing -> {
            standing.setGoalDifference(standing.getGoalsFor() - standing.getGoalsAgainst());
            standing.setLastFive(lastFive(resultsByTeam.get(standing.getTeamId())));
        });

        return standingsByTeam.values()
                .stream()
                .sorted(standingsComparator())
                .toList();
    }

    /**
     * Every team that must appear in the table:
     * 1. All teams registered on the competition (including 0-match teams).
     * 2. Any team appearing in the matches but missing from the competition.
     */
    private List<Team> resolveTeams(Competition competition, List<Match> matches) {
        Map<Long, Team> teams = new LinkedHashMap<>();

        if (competition != null) {
            if (competition.getTeams() != null && !competition.getTeams().isEmpty()) {
                competition.getTeams().forEach(team -> teams.put(team.getId(), team));
            } else if (competition.getId() != null) {
                teamRepository.findByCompetitionsId(competition.getId())
                        .forEach(team -> teams.put(team.getId(), team));
            }
        }

        if (matches != null) {
            for (Match match : matches) {
                if (match.getHomeTeam() != null) {
                    teams.putIfAbsent(match.getHomeTeam().getId(), match.getHomeTeam());
                }
                if (match.getAwayTeam() != null) {
                    teams.putIfAbsent(match.getAwayTeam().getId(), match.getAwayTeam());
                }
            }
        }

        return new ArrayList<>(teams.values());
    }

    private void applyFixtureTotals(TeamStandingDTO home, TeamStandingDTO away,
            int homeScore, int awayScore) {
        home.setPlayed(home.getPlayed() + 1);
        away.setPlayed(away.getPlayed() + 1);

        home.setGoalsFor(home.getGoalsFor() + homeScore);
        home.setGoalsAgainst(home.getGoalsAgainst() + awayScore);

        away.setGoalsFor(away.getGoalsFor() + awayScore);
        away.setGoalsAgainst(away.getGoalsAgainst() + homeScore);
    }

    private TeamStandingDTO ensureStanding(
            Team team,
            Map<Long, TeamStandingDTO> standingsByTeam,
            Map<Long, List<Character>> resultsByTeam) {
        resultsByTeam.computeIfAbsent(team.getId(), ignored -> new ArrayList<>());
        return standingsByTeam.computeIfAbsent(team.getId(), ignored -> newStanding(team));
    }

    private TeamStandingDTO newStanding(Team team) {
        TeamStandingDTO standing = new TeamStandingDTO();
        standing.setTeamId(team.getId());
        standing.setTeamName(team.getName());
        return standing;
    }

    private void applyFixtureResult(
            TeamStandingDTO home,
            TeamStandingDTO away,
            List<Character> homeResults,
            List<Character> awayResults,
            int homeScore,
            int awayScore) {
        if (homeScore > awayScore) {
            home.setWins(home.getWins() + 1);
            away.setLosses(away.getLosses() + 1);
            home.setPoints(home.getPoints() + 3);
            if (homeResults != null) homeResults.add('W');
            if (awayResults != null) awayResults.add('L');
            return;
        }

        if (homeScore < awayScore) {
            away.setWins(away.getWins() + 1);
            home.setLosses(home.getLosses() + 1);
            away.setPoints(away.getPoints() + 3);
            if (homeResults != null) homeResults.add('L');
            if (awayResults != null) awayResults.add('W');
            return;
        }

        home.setDraws(home.getDraws() + 1);
        away.setDraws(away.getDraws() + 1);
        home.setPoints(home.getPoints() + 1);
        away.setPoints(away.getPoints() + 1);
        if (homeResults != null) homeResults.add('D');
        if (awayResults != null) awayResults.add('D');
    }

    @SuppressWarnings("null")
    private Comparator<TeamStandingDTO> standingsComparator() {
        return Comparator.comparingInt(TeamStandingDTO::getPoints).reversed()
                .thenComparing(Comparator.comparingInt(TeamStandingDTO::getGoalDifference).reversed())
                .thenComparing(Comparator.comparingInt(TeamStandingDTO::getGoalsFor).reversed())
                .thenComparing(TeamStandingDTO::getTeamId,
                        Comparator.nullsLast(Comparator.naturalOrder()));
    }

    private String lastFive(List<Character> results) {
        if (results == null || results.isEmpty()) {
            return "";
        }

        int fromIndex = Math.max(0, results.size() - 5);
        return results.subList(fromIndex, results.size())
                .stream()
                .map(String::valueOf)
                .collect(Collectors.joining());
    }

    private int scoreOrZero(Number score) {
        return score == null ? 0 : score.intValue();
    }
}