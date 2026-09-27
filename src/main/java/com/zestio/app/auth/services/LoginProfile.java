package com.zestio.app.auth.services;

import com.zestio.app.auth.RefreshToken;
import com.zestio.app.auth.dto.AuthResponse;
import com.zestio.app.auth.dto.LoginRequest;
import com.zestio.app.auth.security.JwtService;
import com.zestio.app.auth.security.RefreshTokenManager;
import com.zestio.app.profile.Profile;
import com.zestio.app.profile.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class LoginProfile {

        private final AuthenticationManager authenticationManager;
        private final ProfileRepository profileRepository;
        private final JwtService jwtService;
        private final RefreshTokenManager refreshTokenManager;

        @Transactional // <--- ADD THIS ANNOTATION
        public AuthResponse login(LoginRequest request) {
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(request.getPhoneNumber(),
                                                request.getPassword()));

                Profile profile = profileRepository.findByPhoneNumber(request.getPhoneNumber())
                                .orElseThrow();

                String accessToken = jwtService.generateAccessToken(
                                profile.getPhoneNumber(),
                                Map.of("role", profile.getRole().name(), "profileId", profile.getId()));
                RefreshToken refreshToken = refreshTokenManager.create(profile);

                return new AuthResponse(accessToken, refreshToken.getToken(), "Bearer");
        }
}