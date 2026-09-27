package com.zestio.app.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Transactional // <--- Ensures a transaction is available when called directly
    void deleteByProfileId(Long profileId);

    // Or if using a custom JPQL/SQL query:
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.profile.id = :profileId")
    void deleteByProfileIdCustom(Long profileId);
}