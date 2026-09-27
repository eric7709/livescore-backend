package com.zestio.app.team.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zestio.app.team.Team;
import com.zestio.app.utils.Resolver;

import lombok.RequiredArgsConstructor;

@Transactional
@Service
@RequiredArgsConstructor
public class GetTeam {
    private final Resolver resolver;

    @Transactional(readOnly = true)
    public Team get(Long id) {
        return resolver.resolveTeam(id);
    }

}
