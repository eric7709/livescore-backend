package com.livescore.app.match.services;

import org.springframework.stereotype.Service;

import com.livescore.app.match.MatchRepository;
import com.livescore.app.utils.Resolver;

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
