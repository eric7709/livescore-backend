package com.zestio.app.match;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.zestio.app.match.enums.MatchStatus;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long>, JpaSpecificationExecutor<Match> {

        List<Match> findByCompetitionIdOrderByMatchDateAsc(Long competitionId);

        List<Match> findByCompetitionIdAndStatus(Long competitionId, MatchStatus status);

        // Fetch all matches across all competitions, ordered chronologically
        List<Match> findAllByOrderByMatchDateAsc();

        List<Match> findByMatchDateBetweenOrderByMatchDateAsc(Instant start, Instant end);

        // Fetch matches for a specific competition filtered by MatchStatus
        List<Match> findByStatusOrderByMatchDateAsc(MatchStatus status);

        List<Match> findByCompetitionId(Long competitionId);

        List<Match> findByHomeTeamIdOrAwayTeamId(Long homeTeamId, Long awayTeamId);

        List<Match> findByCompetitionIdAndStatusOrderByMatchDateAsc(Long competitionId, MatchStatus status);

        List<Match> findByCompetitionIdAndStatusOrderByMatchDateDesc(Long competitionId, MatchStatus status);

        List<Match> findByStatus(MatchStatus status);

        @Query("""
                            SELECT m FROM Match m
                            WHERE (m.homeTeam.id = :teamId OR m.awayTeam.id = :teamId)
                              AND m.status = :status
                            ORDER BY m.matchDate DESC
                        """)
        List<Match> findByTeamIdAndStatus(
                        @Param("teamId") Long teamId,
                        @Param("status") MatchStatus status);

        List<Match> findByCompetitionIdAndStatusInOrderByMatchDateAsc(Long competitionId,
                        Collection<MatchStatus> statuses);

        // Last N finished matches for a team, most recent first, regardless of
        // home/away side.
        @Query("SELECT m FROM Match m WHERE m.status = :status " +
                        "AND (m.homeTeam.id = :teamId OR m.awayTeam.id = :teamId) " +
                        "ORDER BY m.matchDate DESC")
        List<Match> findRecentFinishedByTeam(@Param("teamId") Long teamId,
                        @Param("status") MatchStatus status,
                        Pageable pageable);

        // Last N finished meetings between two teams, either side home, most recent
        // first.
        @Query("SELECT m FROM Match m WHERE m.status = :status " +
                        "AND ((m.homeTeam.id = :teamAId AND m.awayTeam.id = :teamBId) " +
                        "  OR (m.homeTeam.id = :teamBId AND m.awayTeam.id = :teamAId)) " +
                        "ORDER BY m.matchDate DESC")
        List<Match> findHeadToHead(@Param("teamAId") Long teamAId,
                        @Param("teamBId") Long teamBId,
                        @Param("status") MatchStatus status,
                        Pageable pageable);

        // ------------------------------------------------------------------
        // Date + status lookups (used to build CompetitionMatchesDTO groupings)
        // ------------------------------------------------------------------

        /**
         * All matches within a date range, regardless of status, with the
         * competition eagerly fetched so callers can group by competition
         * without triggering N+1 lazy loads.
         */
        @Query("""
                            SELECT m FROM Match m
                            JOIN FETCH m.competition c
                            WHERE m.matchDate BETWEEN :start AND :end
                            ORDER BY c.id ASC, m.matchDate ASC
                        """)
        List<Match> findByMatchDateBetweenWithCompetition(@Param("start") Instant start,
                        @Param("end") Instant end);

        /**
         * Matches within a date range filtered by one or more statuses, with
         * the competition eagerly fetched. Pass an empty/null collection at
         * the service layer to fall back to
         * {@link #findByMatchDateBetweenWithCompetition}.
         */
        @Query("""
                            SELECT m FROM Match m
                            JOIN FETCH m.competition c
                            WHERE m.matchDate BETWEEN :start AND :end
                              AND m.status IN :statuses
                            ORDER BY c.id ASC, m.matchDate ASC
                        """)
        List<Match> findByMatchDateBetweenAndStatusInWithCompetition(@Param("start") Instant start,
                        @Param("end") Instant end,
                        @Param("statuses") Collection<MatchStatus> statuses);

        @Query("SELECT DISTINCT m FROM Match m " +
                        "LEFT JOIN FETCH m.competition " +
                        "LEFT JOIN FETCH m.homeTeam " +
                        "LEFT JOIN FETCH m.awayTeam")
        List<Match> findAllWithCompetition();

        @Query("SELECT DISTINCT m FROM Match m " +
                        "LEFT JOIN FETCH m.competition " +
                        "LEFT JOIN FETCH m.homeTeam " +
                        "LEFT JOIN FETCH m.awayTeam " +
                        "WHERE m.status IN :statuses")
        List<Match> findByStatusInWithCompetition(List<MatchStatus> statuses);
}