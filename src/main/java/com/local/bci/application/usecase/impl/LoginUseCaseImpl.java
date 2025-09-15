package com.local.bci.application.usecase.impl;

import com.local.bci.application.dto.UserResponseDTO;
import com.local.bci.application.mapper.UserDtoModelMapper;
import com.local.bci.application.usecase.LoginUseCase;
import com.local.bci.domain.model.UserModel;
import com.local.bci.domain.port.persistence.FindUserByTokenPort;
import com.local.bci.domain.port.persistence.SaveUserPort;
import com.local.bci.infrastructure.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementation of {@link LoginUseCase} that handles user login via JWT token.
 * <p>
 * Validates the token, retrieves the corresponding user, updates last login,
 * generates a new token, persists the user, and returns updated user data.
 * </p>
 */
@Service
@Slf4j
public class LoginUseCaseImpl implements LoginUseCase {

    private final FindUserByTokenPort findUserByTokenPort;
    private final SaveUserPort saveUserPort;
    private final JwtService jwtService;
    private final UserDtoModelMapper dtoMapper;

    /**
     * Creates a new instance of the login use case implementation.
     *
     * @param findUserByTokenPort port to retrieve a user by token
     * @param saveUserPort port to persist updated user data
     * @param jwtService service to validate and generate JWT tokens
     * @param dtoMapper mapper for converting between models and DTOs
     */
    public LoginUseCaseImpl(FindUserByTokenPort findUserByTokenPort,
                            SaveUserPort saveUserPort,
                            JwtService jwtService,
                            UserDtoModelMapper dtoMapper) {
        this.findUserByTokenPort = findUserByTokenPort;
        this.saveUserPort = saveUserPort;
        this.jwtService = jwtService;
        this.dtoMapper = dtoMapper;
    }

    /**
     * Processes a login request based on a JWT token.
     *
     * @param token JWT token provided by the client
     * @return a {@link UserResponseDTO} with refreshed token and user data
     * @throws IllegalArgumentException if the token is invalid or mismatched
     * @throws IllegalStateException if no user is found for the token
     */
    @Transactional
    @Override
    public UserResponseDTO apply(String token) {
        log.info("Login with token");

        if (token == null || token.isBlank() || !jwtService.validateToken(token)) {
            throw new IllegalArgumentException("Invalid token");
        }

        String subject = jwtService.getSubject(token);
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Invalid token subject");
        }

        UserModel userModel = findUserByTokenPort.findByToken(token)
                .orElseThrow(() -> new IllegalStateException("User not found for token"));

        if (!subject.equals(userModel.getEmail())) {
            throw new IllegalArgumentException("Token subject mismatch");
        }

        if (!userModel.getIsActive()) {
            throw new IllegalStateException("Cannot login inactive user");
        }

        userModel.setLastLogin(LocalDateTime.now());
        String newToken = jwtService.generateToken(userModel.getEmail());
        userModel.setToken(newToken);

        UserModel saved = saveUserPort.apply(userModel);

        UserResponseDTO resp = dtoMapper.modelToResponseDto(saved);
        resp.setPassword(saved.getPasswordEncrypted());

        return resp;
    }
}
