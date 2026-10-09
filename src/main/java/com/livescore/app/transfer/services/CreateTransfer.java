package com.livescore.app.transfer.services;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.livescore.app.profile.Profile;
import com.livescore.app.profile.ProfileRepository;
import com.livescore.app.team.Team;
import com.livescore.app.team.TeamRepository;
import com.livescore.app.transfer.Transfer;
import com.livescore.app.transfer.TransferRepository;
import com.livescore.app.transfer.dtos.TransferRequestDTO;
import com.livescore.app.transfer.dtos.TransferResponseDTO;
import com.livescore.app.transfer.enums.TransferType;
import com.livescore.app.transfer.utils.TransferMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateTransfer {

    private final TransferRepository transferRepository;
    private final ProfileRepository profileRepository;
    private final TeamRepository teamRepository;
    private final TransferMapper transferMapper;

    // -------------------------------------------------------------------------
    // Entry point
    // -------------------------------------------------------------------------

    public TransferResponseDTO execute(TransferRequestDTO request) {
        validate(request);

        Profile player = findPlayer(request.getPlayerId());
        Team to = findTeam(request.getToTeamId());
        Team from = player.getTeam();

        validateDifferentTeams(from, to);
        validateSameLeague(from, to);

        Integer squadNumber = resolveSquadNumber(player, to, request.getNewSquadNumber());

        Transfer transfer = buildTransfer(request, player, from, to);
        movePlayer(player, to, squadNumber);

        return transferMapper.toDTO(transferRepository.save(transfer));
    }

    // -------------------------------------------------------------------------
    // Lookups
    // -------------------------------------------------------------------------

    private Profile findPlayer(Long playerId) {
        return profileRepository.findById(playerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));
    }

    private Team findTeam(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "To team not found"));
    }

    // -------------------------------------------------------------------------
    // Building and applying the transfer
    // -------------------------------------------------------------------------

    private Transfer buildTransfer(TransferRequestDTO request, Profile player, Team from, Team to) {
        Transfer transfer = new Transfer();
        transfer.setLeague(to.getLeague());
        transfer.setPlayer(player);
        transfer.setFrom(from);
        transfer.setTo(to);
        transfer.setTransferType(request.getTransferType());
        transfer.setFee(normalizeFee(request));
        transfer.setTransferDate(request.getTransferDate());
        return transfer;
    }

    private void movePlayer(Profile player, Team to, Integer squadNumber) {
        player.setTeam(to);
        player.setSquadNumber(squadNumber);
        profileRepository.save(player);
    }

    /** Free transfers carry no fee. */
    private BigDecimal normalizeFee(TransferRequestDTO request) {
        return request.getTransferType() == TransferType.FREE ? null : request.getFee();
    }

    // -------------------------------------------------------------------------
    // Squad number
    // -------------------------------------------------------------------------

    /**
     * Decides which squad number the player carries at the destination team.
     *
     * - A new number, if supplied, is used and must be free.
     * - Otherwise the current number is kept and must be free at the destination.
     * - If the player has no number, it stays null.
     */
    private Integer resolveSquadNumber(Profile player, Team to, Integer newSquadNumber) {
        Integer candidate = newSquadNumber != null ? newSquadNumber : player.getSquadNumber();

        if (candidate == null) {
            return null;
        }

        if (profileRepository.existsByTeamAndSquadNumber(to, candidate)) {
            throw conflict(to, candidate);
        }

        return candidate;
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    private void validate(TransferRequestDTO request) {
        if (request == null) {
            throw bad("Request body is required");
        }
        if (request.getPlayerId() == null) {
            throw bad("Player is required");
        }
        if (request.getToTeamId() == null) {
            throw bad("To team is required");
        }
        if (request.getTransferType() == null) {
            throw bad("Transfer type is required");
        }
        if (request.getTransferDate() == null) {
            throw bad("Transfer date is required");
        }
        if (request.getFee() != null && request.getFee().compareTo(BigDecimal.ZERO) < 0) {
            throw bad("Fee cannot be negative");
        }
        if (request.getNewSquadNumber() != null && request.getNewSquadNumber() < 0) {
            throw bad("Squad number cannot be negative");
        }
    }

    private void validateDifferentTeams(Team from, Team to) {
        if (from != null && to != null && from.getId().equals(to.getId())) {
            throw bad("From team and to team must be different");
        }
    }

    private void validateSameLeague(Team from, Team to) {
        if (from == null || to == null) {
            return;
        }
        if (!from.getLeague().getId().equals(to.getLeague().getId())) {
            throw bad("Player cannot be transferred between different leagues");
        }
    }

    // -------------------------------------------------------------------------
    // Error helpers
    // -------------------------------------------------------------------------

    private ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException conflict(Team to, Integer squadNumber) {
        return new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Squad number " + squadNumber + " is already taken at " + to.getName());
    }
}