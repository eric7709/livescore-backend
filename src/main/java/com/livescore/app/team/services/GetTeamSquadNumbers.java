package com.livescore.app.team.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.livescore.app.team.Team;
import com.livescore.app.utils.Resolver;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor 
@Service 
public class GetTeamSquadNumbers {
    private final Resolver resolver;

    public List<Integer> get(Long teamId){
        Team team = resolver.resolveTeam(teamId);
        return team.getPlayers().stream().map(el -> el.getSquadNumber()).toList();
    }
    
}
