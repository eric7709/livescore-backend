package com.livescore.app.transfer.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.livescore.app.transfer.Transfer;
import com.livescore.app.transfer.TransferRepository;
import com.livescore.app.transfer.dtos.TransferResponseDTO;
import com.livescore.app.transfer.utils.TransferMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetTransfer {

    private final TransferRepository transferRepository;
    private final TransferMapper transferMapper;

    public TransferResponseDTO execute(Long id) {
        Transfer transfer = transferRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found"));
        return transferMapper.toDTO(transfer);
    }
}
