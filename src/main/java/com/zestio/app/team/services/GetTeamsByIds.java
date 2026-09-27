package com.zestio.app.team.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zestio.app.team.Team;
import com.zestio.app.team.TeamRepository;

import lombok.RequiredArgsConstructor;

@Transactional
@Service
@RequiredArgsConstructor
public class GetTeamsByIds {
    private final TeamRepository teamRepository;

    @Transactional(readOnly = true)
    public List<Team> get(List<Long> ids) {
        return teamRepository.findAllById(ids);
    }

}
