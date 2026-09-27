package com.zestio.app.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProfileStatsDTO {
    private long totalProfiles;
    private long totalPlayers;
    private long totalStaff;
    private long totalManagers;
}