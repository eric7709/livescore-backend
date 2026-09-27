package com.zestio.app.team.dto;

import java.util.List;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TeamSquadAndManager {
    private List<Player> squad;
    private Manager manager;
}
