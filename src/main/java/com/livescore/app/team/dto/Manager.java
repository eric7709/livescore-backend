package com.livescore.app.team.dto;

import com.livescore.app.profile.Profile;
import com.livescore.app.profile.enums.PlayerStatus;
import lombok.Data;

@Data
public class Manager {
    private Long id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String fullName;
    private String avatarUrl;
    private PlayerStatus status = PlayerStatus.ACTIVE;
    private String teamName;
    private Long teamId;
    private String teamLogoUrl;

    public static Manager toDTO(Profile profile) {
        Manager manager = new Manager();
        manager.setId(profile.getId());
        manager.setFirstName(profile.getFirstName());
        manager.setLastName(profile.getLastName());
        manager.setPhoneNumber(profile.getPhoneNumber());
        manager.setFullName(profile.getFullName());
        manager.setAvatarUrl(profile.getAvatarUrl());
        manager.setStatus(profile.getStatus());
        manager.setTeamLogoUrl(profile.getTeam().getLogoUrl());
        if (profile.getTeam() != null) {
            manager.setTeamName(profile.getTeam().getName());
            manager.setTeamId(profile.getTeam().getId());
        }
        return manager;
    }
}