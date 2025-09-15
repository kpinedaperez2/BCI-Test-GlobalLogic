package com.local.bci.infrastructure.persistence.adapter;

import com.local.bci.domain.model.UserModel;
import com.local.bci.domain.port.persistence.FindUserByEmailPort;
import com.local.bci.domain.port.persistence.FindUserByTokenPort;
import com.local.bci.domain.port.persistence.SaveUserPort;
import com.local.bci.infrastructure.exception.UserPersistenceException;
import com.local.bci.infrastructure.mapper.UserEntityModelMapper;
import com.local.bci.infrastructure.persistence.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adapter that bridges domain ports with the persistence layer.
 * <p>
 * Implements {@link SaveUserPort}, {@link FindUserByEmailPort}, and {@link FindUserByTokenPort}
 * using a JPA repository and a mapper to convert between entities and domain models.
 * Handles database exceptions and wraps them in {@link UserPersistenceException}.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryAdapter implements SaveUserPort, FindUserByEmailPort, FindUserByTokenPort {

    private final UserJpaRepository jpaRepository;
    private final UserEntityModelMapper mapper;

    /**
     * Saves a user in the persistence layer.
     *
     * @param userModel domain model of the user
     * @return the saved {@link UserModel}
     * @throws UserPersistenceException if a database error occurs
     */
    @Override
    public UserModel apply(UserModel userModel) {
        try {
            return mapper.entityToModel(
                    jpaRepository.save(mapper.modelToEntity(userModel))
            );
        } catch (DataAccessException e) {
            log.error("Error saving user to database: {}", e.getMessage(), e);
            throw new UserPersistenceException("Failed to save user", e);
        }
    }

    /**
     * Finds a user by email.
     *
     * @param email user email
     * @return an {@link Optional} containing the {@link UserModel} if found
     * @throws UserPersistenceException if a database error occurs
     */
    @Override
    public Optional<UserModel> findByEmail(String email) {
        try {
            return jpaRepository.findByEmail(email)
                    .map(mapper::entityToModel);
        } catch (DataAccessException e) {
            log.error("Error finding user by email {}: {}", email, e.getMessage(), e);
            throw new UserPersistenceException("Failed to find user by email", e);
        }
    }

    /**
     * Finds a user by JWT token.
     *
     * @param token user token
     * @return an {@link Optional} containing the {@link UserModel} if found
     * @throws UserPersistenceException if a database error occurs
     */
    @Override
    public Optional<UserModel> findByToken(String token) {
        try {
            return jpaRepository.findByToken(token)
                    .map(mapper::entityToModel);
        } catch (DataAccessException e) {
            log.error("Error finding user by token {}: {}", token, e.getMessage(), e);
            throw new UserPersistenceException("Failed to find user by token", e);
        }
    }
}
