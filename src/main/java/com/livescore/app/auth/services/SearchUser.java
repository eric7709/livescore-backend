package com.livescore.app.auth.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.auth.User;
import com.livescore.app.auth.UserRepository;
import com.livescore.app.auth.dto.UserQueryParams;
import com.livescore.app.auth.utils.UserSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchUser {

private final UserRepository userRepository;

public Page<User> execute(
        UserQueryParams request,
        Pageable pageable
) {

    return userRepository.findAll(
            UserSpecification.search(request),
            pageable
    );
}

}
