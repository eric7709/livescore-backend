package com.zestio.app.matchEvents;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.zestio.app.competition.dto.PlayerRankingDTO;
import com.zestio.app.match.enums.MatchPeriod;
import com.zestio.app.matchEvents.enums.EventType;

import java.util.List;
import java.util.Optional;

public interface MatchEventRepository extends JpaRepository<MatchEvent, Long> {

    List<MatchEvent> findByPrimaryPlayerId(Long playerId);
 
    List<MatchEvent> findByPrimaryPlayerIdAndEventType(Long playerId, EventType eventType);

    List<MatchEvent> findByMatchId(Long matchId);

    Optional<MatchEvent> findByMatchIdAndEventType(Long matchId, EventType eventType);

    List<MatchEvent> findByTeamId(Long teamId);

    List<MatchEvent> findAllByMatchIdAndEventType(Long matchId, EventType eventType);

    boolean existsByMatchIdAndTeamIdAndEventTypeAndPrimaryPlayerIdAndSecondaryPlayerIdAndMinuteGreaterThanEqual(
            Long matchId, Long teamId, EventType eventType, Long primaryPlayerId, Long secondaryPlayerId,
            Integer minute);

    // Traverses MatchEvent -> match -> competition.id, and filters by a set of
    // event types
    List<MatchEvent> findByMatch_CompetitionIdAndEventTypeIn(Long competitionId, List<EventType> eventTypes);

    long countByTeamId(Long teamId);

    long countByPeriod(MatchPeriod period);

    long countByEventType(EventType eventType);

    long countByTeamIdAndPeriod(Long teamId, MatchPeriod period);

    @Query("""
            SELECT e
            FROM MatchEvent e
            WHERE e.match.id = :matchId
            AND (
                e.primaryPlayer.id = :playerId
                OR e.secondaryPlayer.id = :playerId
            )
            """)
    List<MatchEvent> findPlayerEvents(Long matchId, Long playerId);

    long countByTeamIdAndEventType(Long teamId, EventType eventType);

    long countByPeriodAndEventType(MatchPeriod period, EventType eventType);

    long countByTeamIdAndPeriodAndEventType(Long teamId, MatchPeriod period, EventType eventType);

    long countByMatchIdAndEventTypeAndPrimaryPlayerId(Long matchId, EventType eventType, Long primaryPlayerId);

    boolean existsByMatchIdAndEventTypeAndPrimaryPlayerId(Long matchId, EventType eventType, Long primaryPlayerId);

    boolean existsByMatchIdAndEventTypeAndSecondaryPlayerId(Long matchId, EventType eventType, Long primaryPlayerId);

    long countByMatchIdAndEventTypeAndSecondaryPlayerId(Long matchId, EventType eventType, Long secondaryPlayerId);

    long countByMatchIdAndPrimaryPlayerIdAndEventTypeIn(Long matchId, Long primaryPlayerId, List<EventType> eventTypes);

    @Query("""
                SELECT new com.zestio.app.competition.dto.PlayerRankingDTO(
                    p.id, p.fullName, t.id, t.name, t.logoUrl, COUNT(e)
                )
                FROM MatchEvent e
                JOIN e.primaryPlayer p
                JOIN e.team t
                WHERE e.match.competition.id = :competitionId
                  AND e.eventType = :eventType
                GROUP BY p.id, p.fullName, t.id, t.name, t.logoUrl
                ORDER BY COUNT(e) DESC
            """)
    List<PlayerRankingDTO> findPlayerRankingByEventType(
            @Param("competitionId") Long competitionId,
            @Param("eventType") EventType eventType,
            Pageable pageable);

    @Query("""
                SELECT new com.zestio.app.competition.dto.PlayerRankingDTO(
                    p.id, p.fullName, t.id, t.name, t.logoUrl, COUNT(e)
                )
                FROM MatchEvent e
                JOIN e.primaryPlayer p
                JOIN e.team t
                WHERE e.match.competition.id = :competitionId
                  AND e.eventType IN :eventTypes
                GROUP BY p.id, p.fullName, t.id, t.name, t.logoUrl
                ORDER BY COUNT(e) DESC
            """)
    List<PlayerRankingDTO> findPlayerRankingByEventTypes(
            @Param("competitionId") Long competitionId,
            @Param("eventTypes") List<EventType> eventTypes,
            Pageable pageable);

}