package com.zestio.app.matchLineup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface MatchLineupRepository extends JpaRepository<MatchLineup, Long> {

    void deleteByMatchIdAndTeamId(Long matchId, Long teamId);

    List<MatchLineup> findByMatchId(Long matchId);

    Optional<MatchLineup> findByMatchIdAndTeamId(Long matchId, Long teamId);

    boolean existsByMatchIdAndTeamId(Long matchId, Long teamId);

}
