package com.livescore.app.profile;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.livescore.app.auth.enums.Role;
import com.livescore.app.profile.enums.CaptainStatus;
import com.livescore.app.team.Team;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long>, JpaSpecificationExecutor<Profile> {
    long countByRole(Role role);

    List<Profile> findByTeamId(Long teamId);

    boolean existsByTeamAndSquadNumberAndIdNot(Team team, Integer squadNumber, Long excludeId);

    boolean existsByTeamAndSquadNumber(Team team, Integer squadNumber);

    List<Profile> findByTeamIdAndRole(Long teamId, Role role);

    Optional<Profile> findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<Profile> findByTeamIdAndCaptainStatus(Long teamId, CaptainStatus captainStatus);
}