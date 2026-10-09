package com.livescore.app.team.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.team.Team;
import com.livescore.app.team.TeamRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class GetAllTeams {
    private final TeamRepository teamRepository;
    
    @Transactional(readOnly = true)
    public List<Team> get() {
        return teamRepository.findAll();
    }

}
