package com.local.bci.application.usecase;

import com.local.bci.application.dto.SignUpRequestDTO;
import com.local.bci.application.dto.UserResponseDTO;

import java.util.function.Function;

/**
 * Represents a use case for handling user sign-up operations.
 * <p>
 * This interface extends the {@link java.util.function.Function} interface,
 * taking a {@link SignUpRequestDTO} as input (containing the user's registration data)
 * and producing a {@link UserResponseDTO} as the output after successful sign-up.
 * </p>
 */
public interface SingUpUseCase extends Function<SignUpRequestDTO, UserResponseDTO> {

}
