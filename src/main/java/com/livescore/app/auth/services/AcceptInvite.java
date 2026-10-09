package com.livescore.app.auth.services;

import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.auth.tempUserAndPasswordToken.TempUser;
import com.livescore.app.auth.tempUserAndPasswordToken.TempUserRepository;
import com.livescore.app.auth.User;
import com.livescore.app.auth.UserRepository;
import com.livescore.app.auth.enums.Role;
import com.livescore.app.auth.utils.PasswordUtils;
import com.livescore.app.exceptions.BadRequestException;
import com.livescore.app.profile.Profile;
import com.livescore.app.profile.ProfileRepository;
import com.livescore.app.profile.enums.Position;
import com.livescore.app.team.Team;
import com.livescore.app.team.TeamRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AcceptInvite {

    /**
     * Roles that get a User only, with no Profile. ADMIN is here because a
     * Profile requires a league and admins belong to none.
     */
    private static final Set<Role> USER_ONLY_ROLES = Set.of(Role.MODERATOR, Role.ADMIN);

    /** Roles that are allowed to have no league. */
    private static final Set<Role> LEAGUELESS_ROLES = Set.of(Role.ADMIN);

    private final PasswordUtils passwordUtils;
    private final UserRepository userRepository;
    private final TempUserRepository tempUserRepository;
    private final ProfileRepository profileRepository;
    private final TeamRepository teamRepository;

    @Transactional
    public void execute(String password, String token) {
        TempUser tempUser = tempUserRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired link"));
        if (!tempUser.isEnabled() || tempUser.isExpired()) {
            throw new BadRequestException("Invalid or expired link");
        }
        if (userRepository.findByEmail(tempUser.getEmail()).isPresent()) {
            throw new BadRequestException("User already exists");
        }

        Role role = tempUser.getRole();
        if (role == null
                || (tempUser.getLeague() == null && !LEAGUELESS_ROLES.contains(role))) {
            throw new BadRequestException("Invalid or expired link");
        }

        User user = new User();
        user.setEmail(tempUser.getEmail());
        user.setFirstName(tempUser.getFirstName());
        user.setLastName(tempUser.getLastName());
        user.setPassword(passwordUtils.encode(password));
        user.setRole(role);
        user.setLeague(tempUser.getLeague()); // null for ADMIN

        if (!USER_ONLY_ROLES.contains(role)) {
            Profile profile = new Profile();
            profile.setFirstName(tempUser.getFirstName());
            profile.setLastName(tempUser.getLastName());
            profile.setRole(role);
            profile.setLeague(tempUser.getLeague());
            profile.setTeam(tempUser.getTeam());
            if (role == Role.MANAGER) {
                profile.setPosition(Position.MANAGER);
            }
            profile = profileRepository.save(profile);

            if (role == Role.MANAGER && tempUser.getTeam() != null) {
                Team team = tempUser.getTeam();
                team.setManager(profile);
                teamRepository.save(team);
            }

            user.setProfile(profile);
        }

        userRepository.save(user);
        tempUserRepository.delete(tempUser); // one-time link
    }
}