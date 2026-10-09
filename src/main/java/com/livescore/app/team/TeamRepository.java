package com.livescore.app.team;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    // Duplicate check
    boolean existsByNameIgnoreCase(String name);

    // League-scoped or global paginated search
    @Query("""
            SELECT t
            FROM Team t
            WHERE (:leagueId IS NULL OR t.league.id = :leagueId)
            AND (
                :query = ''
                OR LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(t.teamCode) LIKE LOWER(CONCAT('%', :query, '%'))
            )
            """)
    Page<Team> searchByLeague(
            @Param("leagueId") Long leagueId,
            @Param("query") String query,
            Pageable pageable);

    // Competition teams
    List<Team> findByCompetitionsId(Long competitionId);

    // Bulk duplicate check
    @Query("SELECT COUNT(t) > 0 FROM Team t WHERE LOWER(t.name) IN :names")
    boolean existsByNameInIgnoreCase(List<String> names);

    List<Team> findByNameContainingIgnoreCaseOrTeamCodeContainingIgnoreCase(
            String name,
            String code);

    // Find teams by names
    List<Team> findByNameIn(List<String> names);
}