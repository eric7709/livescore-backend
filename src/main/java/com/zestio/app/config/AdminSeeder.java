package com.zestio.app.config;

import com.zestio.app.profile.Profile;
import com.zestio.app.profile.ProfileRepository;
import com.zestio.app.profile.enums.CaptainStatus;
import com.zestio.app.profile.enums.PlayerStatus;
import com.zestio.app.profile.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (profileRepository.existsByPhoneNumber("0000000000")) {
            return; // already seeded
        }
        Profile admin = new Profile();
        admin.setFirstName("System");
        admin.setLastName("Admin");
        admin.setPhoneNumber("0000000000"); // change before real deployment
        admin.setPassword(passwordEncoder.encode("ChangeMe123!")); // change immediately after first login
        admin.setStatus(PlayerStatus.ACTIVE);
        admin.setCaptainStatus(CaptainStatus.NONE);
        admin.setRole(Role.ADMIN);
        profileRepository.save(admin);
    }
}