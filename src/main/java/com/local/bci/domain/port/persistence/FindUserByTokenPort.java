package com.local.bci.domain.port.persistence;

import com.local.bci.domain.model.UserModel;

import java.util.Optional;

@FunctionalInterface
public interface FindUserByTokenPort {
    Optional<UserModel> findByToken(String token);
}
