package com.livescore.app.auth.tempUserAndPasswordToken;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TempUserRepository extends JpaRepository<TempUser, Long> {
    Optional<TempUser> findByEmail(String email);

    Optional<TempUser> findByToken(String token);
}
