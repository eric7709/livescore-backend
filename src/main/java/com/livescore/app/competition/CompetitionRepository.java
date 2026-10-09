package com.livescore.app.competition;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.livescore.app.competition.enums.CompetitionStatus;
import com.livescore.app.competition.enums.CompetitionType;

import java.time.LocalDate;
import java.util.List;

public interface CompetitionRepository extends JpaRepository<Competition, Long>, JpaSpecificationExecutor<Competition> {

    List<Competition> findByStatus(CompetitionStatus status);

    List<Competition> findByCompetitionType(CompetitionType competitionType);

    @Query("SELECT DISTINCT c FROM Competition c JOIN c.matches m WHERE DATE(m.matchDate) = :date")
    List<Competition> findCompetitionsByMatchDate(@Param("date") LocalDate date);

}