package com.local.bci.application.usecase;

import com.local.bci.application.dto.UserResponseDTO;

import java.util.function.Function;

/**
 * Represents a use case for handling user login operations.
 * <p>
 * This interface extends the {@link java.util.function.Function} interface,
 * taking a {@code String} as input (e.g., a username, email, or token)
 * and producing a {@link UserResponseDTO} as the output.
 * </p>
 */
public interface LoginUseCase extends Function<String, UserResponseDTO> {
}
