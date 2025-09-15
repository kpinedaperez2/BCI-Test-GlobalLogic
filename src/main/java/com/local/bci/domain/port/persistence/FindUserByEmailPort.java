package com.local.bci.domain.port.persistence;

import com.local.bci.domain.model.UserModel;

import java.util.Optional;

@FunctionalInterface
public interface FindUserByEmailPort {
    Optional<UserModel> findByEmail(String email);
}
