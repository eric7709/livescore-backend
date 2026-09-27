package com.zestio.app.team;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    // ✅ single duplicate check (case insensitive)
    boolean existsByNameIgnoreCase(String name);

    Page<Team> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<Team> findByCompetitionsId(Long competitionId);

    List<Team> findByNameContainingIgnoreCaseOrTeamCodeContainingIgnoreCase(String name, String code);

    // ✅ bulk duplicate check
    @Query("SELECT COUNT(t) > 0 FROM Team t WHERE LOWER(t.name) IN :names")
    boolean existsByNameInIgnoreCase(List<String> names);

    // ✅ optional (useful for debugging or validation)
    List<Team> findByNameIn(List<String> names);
}