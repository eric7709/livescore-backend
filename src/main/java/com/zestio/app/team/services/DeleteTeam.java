package com.zestio.app.team.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zestio.app.team.TeamRepository;
import com.zestio.app.utils.Resolver;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class DeleteTeam {
    private final Resolver resolver;
    private final TeamRepository teamRepository;

    @Transactional
    public void delete(Long id) {
        teamRepository.delete(resolver.resolveTeam(id));
    }
}
