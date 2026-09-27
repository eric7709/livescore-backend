package com.zestio.app.auth;

import com.zestio.app.auth.dto.AuthResponse;
import com.zestio.app.auth.dto.CreateInviteRequest;
import com.zestio.app.auth.dto.InviteResponse;
import com.zestio.app.auth.dto.LoginRequest;
import com.zestio.app.auth.dto.ProfileDetailsResponse;
import com.zestio.app.auth.dto.RefreshRequest;
import com.zestio.app.auth.dto.RegisterRequest;
import com.zestio.app.auth.services.GenerateInviteCode;
import com.zestio.app.auth.services.GetCurrentProfile;
import com.zestio.app.auth.services.LoginProfile;
import com.zestio.app.auth.services.LogoutProfile;
import com.zestio.app.auth.services.RefreshAccessToken;
import com.zestio.app.auth.services.RegisterProfile;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final GetCurrentProfile getCurrentProfile;
    private final GenerateInviteCode generateInviteCode;
    private final RegisterProfile registerProfile;
    private final LoginProfile loginProfile;
    private final RefreshAccessToken refreshAccessToken;
    private final LogoutProfile logoutProfile;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody RegisterRequest request) {
        registerProfile.register(request);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return loginProfile.login(request);
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return refreshAccessToken.refresh(request.getRefreshToken());
    }

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public ProfileDetailsResponse me() {
        return ProfileDetailsResponse.toDTO(getCurrentProfile.get());
    }

    @PostMapping("/invites")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public InviteResponse createInvite(@Valid @RequestBody CreateInviteRequest request) {
        return InviteResponse.toDTO(generateInviteCode.generate(request));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public void logout(@Valid @RequestBody RefreshRequest request) {
        logoutProfile.logout(request.getRefreshToken());
    }
}