package com.livescore.app.auth;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.livescore.app.auth.dto.AcceptInviteRequestDTO;
import com.livescore.app.auth.dto.ChangePasswordRequestDTO;
import com.livescore.app.auth.dto.ForgotPasswordRequestDTO;
import com.livescore.app.auth.dto.LoginRequestDTO;
import com.livescore.app.auth.dto.LoginResult;
import com.livescore.app.auth.dto.MeResponseDTO;
import com.livescore.app.auth.dto.ResetPasswordRequestDTO;
import com.livescore.app.auth.dto.TempUserDTO;
import com.livescore.app.auth.dto.TempUserResponseDTO;
import com.livescore.app.auth.dto.UpdateUserNameRequestDTO;
import com.livescore.app.auth.dto.UserDTO;
import com.livescore.app.auth.dto.UserQueryParams;
import com.livescore.app.security.CookieService;
import com.livescore.app.auth.services.AcceptInvite;
import com.livescore.app.auth.services.ChangePassword;
import com.livescore.app.auth.services.CreateInviteLink;
import com.livescore.app.auth.services.ForgotPassword;
import com.livescore.app.auth.services.GetMe;
import com.livescore.app.auth.services.GetTempUserFromToken;
import com.livescore.app.auth.services.LoginUser;
import com.livescore.app.auth.services.RefreshTokenService;
import com.livescore.app.auth.services.ResetPassword;
import com.livescore.app.auth.services.SearchUser;
import com.livescore.app.auth.services.UpdateUsersName;
import com.livescore.app.exceptions.BadRequestException;
import com.livescore.app.security.CurrentUser;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final LoginUser loginUser;
    private final RefreshTokenService refreshTokenService;
    private final GetMe getMe;
    private final SearchUser searchUser;
    private final CreateInviteLink createInviteLink;
    private final GetTempUserFromToken getTempUserFromToken;
    private final AcceptInvite acceptInvite;
    private final CookieService cookieService;
    private final ForgotPassword forgotPassword;
    private final ResetPassword resetPassword;
    private final ChangePassword changePassword;
    private final UpdateUsersName updateUsersName;
    private final CurrentUser currentUser;

    // ================= SESSION =================

    /** Tokens go out as httpOnly cookies; the body is just the user. */
    @PostMapping("/login")
    public LoginResult login(@Valid @RequestBody LoginRequestDTO dto, HttpServletResponse response) {
        LoginResult result = loginUser.execute(dto);
        cookieService.addTokenCookies(response, result.tokens());
        return result;
    }

    @PatchMapping("/me/name")
    public ResponseEntity<Void> updateName(
            @Valid @RequestBody UpdateUserNameRequestDTO request) {
        updateUsersName.execute(
                currentUser.email(),
                request.getFirstName(),
                request.getLastName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
            @CookieValue(name = CookieService.REFRESH_COOKIE, required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken == null || refreshToken.isBlank()) {
            return unauthorized(response);
        }
        try {
            cookieService.addTokenCookies(response, refreshTokenService.execute(refreshToken));
            return ResponseEntity.noContent().build();
        } catch (BadRequestException e) {
            return unauthorized(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        cookieService.clearTokenCookies(response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public MeResponseDTO me() {
        return getMe.execute(currentUser.email());
    }

    // ================= INVITES =================

    @PostMapping("/invites")
    public TempUserResponseDTO createInvite(@Valid @RequestBody TempUserDTO dto) {
        return TempUserResponseDTO.toDTO(createInviteLink.execute(dto));
    }

    /** Public. */
    @GetMapping("/token/{token}")
    public TempUserResponseDTO getTempUser(@PathVariable String token) {
        return getTempUserFromToken.execute(token);
    }

    /** Public. */
    @PostMapping("/token/{token}/accept")
    public void acceptInvite(@PathVariable String token, @Valid @RequestBody AcceptInviteRequestDTO dto) {
        acceptInvite.execute(dto.getPassword(), token);
    }

    // ================= USERS =================

    @GetMapping("/users")
    public Page<UserDTO> searchUsers(@ModelAttribute UserQueryParams params, Pageable pageable) {
        return searchUser.execute(params, pageable).map(UserDTO::fromEntity);
    }

    // ================= PASSWORDS =================

    /** Public. Always 204, never leaks whether the email exists. */
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        forgotPassword.execute(request);
        return ResponseEntity.noContent().build();
    }

    /** Public. */
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        resetPassword.execute(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequestDTO request) {
        changePassword.execute(currentUser.id(), request);
        return ResponseEntity.noContent().build();
    }

    // ================= HELPERS =================

    private ResponseEntity<Void> unauthorized(HttpServletResponse response) {
        cookieService.clearTokenCookies(response);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}