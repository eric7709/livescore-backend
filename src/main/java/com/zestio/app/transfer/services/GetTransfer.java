package com.zestio.app.transfer.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.zestio.app.transfer.Transfer;
import com.zestio.app.transfer.TransferRepository;
import com.zestio.app.transfer.dtos.TransferResponseDTO;
import com.zestio.app.transfer.helpers.TransferMapper;

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
