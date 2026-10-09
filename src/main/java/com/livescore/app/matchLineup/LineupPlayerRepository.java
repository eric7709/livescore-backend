package com.livescore.app.matchLineup;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LineupPlayerRepository extends JpaRepository<LineupPlayer, Long> {
    List<LineupPlayer> findByMatchLineupMatchId(Long matchId);
    
    List<LineupPlayer> findByPlayerId(Long playerId);
}