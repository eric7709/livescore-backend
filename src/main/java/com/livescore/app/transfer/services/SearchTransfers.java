package com.livescore.app.transfer.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.transfer.Transfer;
import com.livescore.app.transfer.TransferRepository;
import com.livescore.app.transfer.dtos.TransferFilterDTO;
import com.livescore.app.transfer.dtos.TransferPageResponseDTO;
import com.livescore.app.transfer.utils.TransferMapper;
import com.livescore.app.transfer.utils.TransferSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchTransfers {

    private final TransferRepository transferRepository;
    private final TransferMapper transferMapper;

    public TransferPageResponseDTO execute(TransferFilterDTO filter, Pageable pageable) {
        Page<Transfer> page = transferRepository.findAll(
                TransferSpecification.withFilters(filter), pageable);

        return TransferPageResponseDTO.builder()
                .content(page.getContent().stream().map(transferMapper::toDTO).toList())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .page(page.getNumber())
                .size(page.getSize())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
