package com.zestio.app.auth.security;

import com.zestio.app.profile.Profile;
import com.zestio.app.profile.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final ProfileRepository profileRepository;

    @Override
    public UserDetails loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {
        Profile profile = profileRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UsernameNotFoundException("No account found for that phone number"));

        if (profile.getPassword() == null) {
            throw new UsernameNotFoundException("This account does not have login access");
        }

        return User.builder()
                .username(profile.getPhoneNumber())
                .password(profile.getPassword())
                .authorities("ROLE_" + profile.getRole().name())
                .build();
    }
}