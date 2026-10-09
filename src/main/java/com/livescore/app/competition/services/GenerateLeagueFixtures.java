package com.livescore.app.competition.services;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.competition.Competition;
import com.livescore.app.competition.CompetitionRepository;
import com.livescore.app.competition.enums.CompetitionLegFormat;
import com.livescore.app.competition.dto.FixturePairing;
import com.livescore.app.competition.dto.RoundRobinFixtureGenerator;
import com.livescore.app.competition.utils.FixtureGenerationGuard;
import com.livescore.app.match.Match;
import com.livescore.app.match.MatchRepository;
import com.livescore.app.team.Team;

import lombok.RequiredArgsConstructor;

/**
 * Generates a full league schedule (EPL style) for a competition.
 * Single or double round-robin is decided by the competition's legFormat.
 *
 * NOTE: assumes MatchRepository lives at com.livescore.app.match.MatchRepository,
 * mirroring CompetitionRepository. Adjust the import if it's elsewhere.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class GenerateLeagueFixtures {

    private final CompetitionRepository competitionRepository;
    private final MatchRepository matchRepository;

    /**
     * @param competitionId id of the competition to generate fixtures for (its teams must already be set)
     * @param firstKickoff  date/time of the very first round's matches
     * @param roundInterval gap between successive rounds, e.g. Duration.ofDays(7)
     */
    public List<Match> execute(Long competitionId, LocalDateTime firstKickoff, Duration roundInterval) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new IllegalArgumentException("Competition not found: " + competitionId));

        FixtureGenerationGuard.assertCanGenerate(
                competition, !matchRepository.findByCompetitionId(competitionId).isEmpty());

        List<Team> teams = new ArrayList<>(competition.getTeams());
        if (teams.size() < 2) {
            throw new IllegalStateException("Need at least 2 teams to generate fixtures");
        }

        boolean doubleLeg = competition.getLegFormat() == CompetitionLegFormat.DOUBLE;
        List<List<FixturePairing>> schedule = doubleLeg
                ? RoundRobinFixtureGenerator.generateDoubleRoundRobin(teams)
                : RoundRobinFixtureGenerator.generateSingleRoundRobin(teams);

        List<Match> matches = buildMatches(competition, schedule, firstKickoff, roundInterval);

        competition.setTotalTeams(teams.size());
        competition.setTotalRounds(schedule.size());
        competitionRepository.save(competition);

        return matchRepository.saveAll(matches);
    }

    private List<Match> buildMatches(Competition competition, List<List<FixturePairing>> schedule,
                                      LocalDateTime firstKickoff, Duration roundInterval) {
        List<Match> matches = new ArrayList<>();
        LocalDateTime kickoff = firstKickoff;

        int roundNumber = 1;
        for (List<FixturePairing> round : schedule) {
            for (FixturePairing pairing : round) {
                Match match = new Match();
                match.setHomeTeam(pairing.home());
                match.setAwayTeam(pairing.away());
                match.setCompetition(competition);
                match.setMatchDate(kickoff.atZone(ZoneId.systemDefault()).toInstant());
                match.setRound(roundNumber);
                matches.add(match);
            }
            kickoff = kickoff.plus(roundInterval);
            roundNumber++;
        }
        return matches;
    }
}