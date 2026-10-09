package com.livescore.app.competition.services;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.competition.Competition;
import com.livescore.app.competition.CompetitionRepository;
import com.livescore.app.competition.enums.CompetitionStatus;
import com.livescore.app.match.Match;
import com.livescore.app.match.MatchRepository;
import com.livescore.app.match.enums.MatchStatus;
import com.livescore.app.match.enums.MatchType;

import lombok.RequiredArgsConstructor;

/**
 * Call this whenever a match is FINISHED. If it was the LAST unfinished match
 * of its knockout round (for two-legged ties: both legs of every tie), the
 * next round is generated from the winners. If it was the final, the
 * competition is marked COMPLETED. Otherwise it's a no-op, so it's safe to
 * call after every finished match.
 *
 * UpdateMatch calls this automatically when a match moves to FINISHED.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class AdvanceKnockoutStage {

    private static final Duration DEFAULT_GAP_UNTIL_NEXT_ROUND = Duration.ofDays(3);

    private final MatchRepository matchRepository;
    private final CompetitionRepository competitionRepository;
    private final GenerateCupFixtures generateCupFixtures;

    /** Uses a default 3-day gap after the last match of the round. */
    public Optional<List<Match>> advance(Long completedMatchId) {
        return advance(completedMatchId, DEFAULT_GAP_UNTIL_NEXT_ROUND);
    }

    /**
     * @return the newly generated matches, or empty if nothing was generated
     *         (not a knockout match, round not finished yet, or it was the final)
     */
    public Optional<List<Match>> advance(Long completedMatchId, Duration gapUntilNextRound) {
        Match completedMatch = matchRepository.findById(completedMatchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + completedMatchId));

        MatchType type = completedMatch.getMatchType();
        if (type != MatchType.KNOCKOUT && type != MatchType.FINAL) {
            return Optional.empty(); // league / group / friendly matches don't auto-progress
        }
        if (completedMatch.getRound() == null || completedMatch.getCompetition() == null) {
            return Optional.empty();
        }

        Competition competition = completedMatch.getCompetition();
        Long competitionId = competition.getId();
        int round = completedMatch.getRound();

        List<Match> roundMatches = generateCupFixtures.findRoundMatches(competitionId, round);
        boolean roundComplete = roundMatches.stream().allMatch(m -> m.getStatus() == MatchStatus.FINISHED);
        if (!roundComplete) {
            return Optional.empty();
        }

        if (type == MatchType.FINAL) {
            competition.setStatus(CompetitionStatus.COMPLETED);
            competitionRepository.save(competition);
            return Optional.empty();
        }

        if (!generateCupFixtures.findRoundMatches(competitionId, round + 1).isEmpty()) {
            return Optional.empty(); // next round already generated - don't duplicate it
        }

        LocalDateTime lastKickoff = roundMatches.stream()
                .map(Match::getMatchDate)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .map(instant -> LocalDateTime.ofInstant(instant, ZoneId.systemDefault()))
                .orElse(LocalDateTime.now());

        List<Long> matchIds = roundMatches.stream().map(Match::getId).toList();
        List<Match> nextRound = generateCupFixtures.generateNextKnockoutRoundFromResults(
                competitionId, matchIds, lastKickoff.plus(gapUntilNextRound));

        return Optional.of(nextRound);
    }
}
