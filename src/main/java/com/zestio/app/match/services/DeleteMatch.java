package com.zestio.app.match.services;

import org.springframework.stereotype.Service;

import com.zestio.app.match.MatchRepository;
import com.zestio.app.utils.Resolver;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteMatch {
    private final MatchRepository matchRepository;
    private final Resolver resolver;

    public void delete(Long id) {
        matchRepository.delete(resolver.resolveMatch(id));
    }
}
