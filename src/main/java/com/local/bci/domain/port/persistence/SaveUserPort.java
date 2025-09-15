package com.local.bci.domain.port.persistence;

import com.local.bci.domain.model.UserModel;

import java.util.function.Function;

@FunctionalInterface
public interface SaveUserPort extends Function<UserModel, UserModel> {
}
