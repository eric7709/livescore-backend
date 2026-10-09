package com.livescore.app.competition.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.competition.Competition;
import com.livescore.app.competition.CompetitionRepository;
import com.livescore.app.competition.enums.CompetitionStatus;
import com.livescore.app.competition.enums.CompetitionType;
import com.livescore.app.match.Match;
import com.livescore.app.match.MatchRepository;
import com.livescore.app.match.enums.MatchStatus;

import lombok.RequiredArgsConstructor;

/**
 * Keeps a competition's status in step with its matches. Call it after any
 * match's status changes (UpdateMatch does this automatically).
 *
 * SCHEDULED -> ONGOING   when its first match goes LIVE (or is finished
 *                        directly, e.g. a result entered after the fact).
 * ONGOING   -> COMPLETED when the last match of a LEAGUE finishes.
 *
 * Cups / tournaments are NOT completed here: "all generated matches are
 * finished" is also true between knockout rounds, before the next round has
 * been created. AdvanceKnockoutStage marks them COMPLETED when the FINAL
 * finishes.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UpdateCompetitionStatus {

    private final CompetitionRepository competitionRepository;
    private final MatchRepository matchRepository;

    public void sync(Long matchId) {
        Match match = matchRepository.findById(matchId).orElse(null);
        if (match == null || match.getCompetition() == null) {
            return; // friendly - no competition to update
        }

        Competition competition = match.getCompetition();
        CompetitionStatus current = competition.getStatus();
        if (current == CompetitionStatus.CANCELLED || current == CompetitionStatus.COMPLETED) {
            return;
        }

        MatchStatus status = match.getStatus();

        // First match starts -> ONGOING
        if (current == CompetitionStatus.SCHEDULED
                && (status == MatchStatus.LIVE || status == MatchStatus.FINISHED)) {
            competition.setStatus(CompetitionStatus.ONGOING);
            current = CompetitionStatus.ONGOING;
        }

        // Last league match finishes -> COMPLETED
        if (status == MatchStatus.FINISHED
                && competition.getCompetitionType() == CompetitionType.LEAGUE
                && allMatchesDone(competition.getId())) {
            competition.setStatus(CompetitionStatus.COMPLETED);
        }

        competitionRepository.save(competition);
    }

    /** Every match is FINISHED or CANCELLED (a cancelled fixture shouldn't block the season ending). */
    private boolean allMatchesDone(Long competitionId) {
        List<Match> matches = matchRepository.findByCompetitionId(competitionId);
        return !matches.isEmpty() && matches.stream()
                .allMatch(m -> m.getStatus() == MatchStatus.FINISHED
                        || m.getStatus() == MatchStatus.CANCELLED);
    }
}
