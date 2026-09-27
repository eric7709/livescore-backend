package com.zestio.app.profile.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zestio.app.profile.ProfileRepository;
import com.zestio.app.profile.dto.ProfileSummaryResponseDTO;
import com.zestio.app.profile.enums.Role;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetPlayerSummariesByTeam {

    private final ProfileRepository profileRepository;

    public List<ProfileSummaryResponseDTO> get(Long teamId) {

        return profileRepository.findByTeamId(teamId)
                .stream()
                .filter(profile -> profile.getRole() == Role.PLAYER)
                .map(profile -> ProfileSummaryResponseDTO.builder()
                        .id(profile.getId())
                        .fullName(profile.getFullName())
                        .avatarUrl(profile.getAvatarUrl())
                        .position(profile.getPosition())
                        .status(profile.getStatus())
                        .squadNumber(profile.getSquadNumber())
                        .build())
                .toList();
    }
}
