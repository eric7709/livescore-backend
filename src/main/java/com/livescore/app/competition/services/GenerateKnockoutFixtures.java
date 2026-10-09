package com.livescore.app.competition.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.competition.Competition;
import com.livescore.app.competition.CompetitionRepository;
import com.livescore.app.competition.utils.FixtureGenerationGuard;
import com.livescore.app.match.Match;
import com.livescore.app.match.MatchRepository;
import com.livescore.app.team.Team;

import lombok.RequiredArgsConstructor;

/**
 * Pure knockout competition (no group stage): generates round 1 straight from
 * the competition's teams. Every later round is generated automatically by
 * AdvanceKnockoutStage as results come in.
 *
 * If the team count isn't a power of two, the first teams in the draw order
 * get a bye and round 1 is a preliminary round with only the remaining teams.
 * E.g. 6 teams: 2 byes, round 1 = 2 matches among 4 teams, then semi-finals
 * (2 bye teams + 2 winners), then the final.
 *
 * Single or two-legged ties follow the competition's legFormat.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class GenerateKnockoutFixtures {

    private final CompetitionRepository competitionRepository;
    private final MatchRepository matchRepository;
    private final GenerateCupFixtures generateCupFixtures;

    /**
     * @param shuffleTeams true = random draw (so random byes); false = deterministic
     *                     order by team id, so lowest ids get the byes (treat as seeding)
     */
    public List<Match> execute(Long competitionId, LocalDateTime firstKickoff, boolean shuffleTeams) {
        Competition competition = competitionRepository.findById(competitionId)
                .orElseThrow(() -> new IllegalArgumentException("Competition not found: " + competitionId));

        FixtureGenerationGuard.assertCanGenerate(
                competition, !matchRepository.findByCompetitionId(competitionId).isEmpty());

        List<Team> teams = new ArrayList<>(competition.getTeams());
        int n = teams.size();
        if (n < 2) {
            throw new IllegalStateException("Need at least 2 teams to generate fixtures");
        }

        if (shuffleTeams) {
            Collections.shuffle(teams);
        } else {
            teams.sort(Comparator.comparing(Team::getId));
        }

        int bracketSize = Integer.highestOneBit(n - 1) << 1; // next power of two >= n (n >= 2)
        int byes = bracketSize - n;
        List<Team> playingRoundOne = teams.subList(byes, n);

        competition.setTotalTeams(n);
        competition.setTotalRounds(Integer.numberOfTrailingZeros(bracketSize));
        competitionRepository.save(competition);

        return generateCupFixtures.createKnockoutRound(
                competition, playingRoundOne, 1, firstKickoff, n == 2);
    }
}
