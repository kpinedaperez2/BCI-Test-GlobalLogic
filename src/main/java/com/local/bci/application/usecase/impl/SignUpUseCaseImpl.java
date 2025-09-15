package com.local.bci.application.usecase.impl;

import com.local.bci.application.dto.SignUpRequestDTO;
import com.local.bci.application.dto.UserResponseDTO;
import com.local.bci.application.mapper.UserDtoModelMapper;
import com.local.bci.application.usecase.SingUpUseCase;
import com.local.bci.domain.model.UserModel;
import com.local.bci.domain.port.persistence.FindUserByEmailPort;
import com.local.bci.domain.port.persistence.SaveUserPort;
import com.local.bci.infrastructure.exception.IncorrectPatternException;
import com.local.bci.infrastructure.exception.UserExistsException;
import com.local.bci.infrastructure.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Implementation of {@link SingUpUseCase} that manages user registration.
 * <p>
 * Validates the email and password format, ensures the user does not already exist,
 * maps the DTO to the domain model, encrypts the password, generates a JWT token,
 * saves the user, and returns a response DTO.
 * </p>
 */
@Service
@Slf4j
public class SignUpUseCaseImpl implements SingUpUseCase {

    private final UserDtoModelMapper dtoMapper;

    private final SaveUserPort saveUserPort;

    private final FindUserByEmailPort findUserByEmailPort;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    @Value("${app.regex.email}")
    private String emailRegex;

    @Value("${app.regex.password}")
    private String passwordRegex;

    private Pattern emailPattern;
    private Pattern passwordPattern;

    public SignUpUseCaseImpl(UserDtoModelMapper dtoMapper, SaveUserPort saveUserPort, FindUserByEmailPort findUserByEmailPort, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.dtoMapper = dtoMapper;
        this.saveUserPort = saveUserPort;
        this.findUserByEmailPort = findUserByEmailPort;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostConstruct
    private void initPatterns() {
        emailPattern = Pattern.compile(emailRegex);
        passwordPattern = Pattern.compile(passwordRegex);
    }

    /**
     * Processes a sign-up request by validating input, creating a new user,
     * and returning the registered user data.
     *
     * @param request DTO containing user registration details
     * @return a {@link UserResponseDTO} with user information and token
     * @throws IllegalArgumentException if email or password format is invalid
     * @throws IllegalStateException    if a user with the same email already exists
     */
    @Transactional
    @Override
    public UserResponseDTO apply(SignUpRequestDTO request) {
        log.info("SignUp request for email={}", request.getEmail());

        if (request.getEmail() == null || !emailPattern.matcher(request.getEmail()).matches()) {
            throw new IncorrectPatternException("Invalid email format");
        }

        if (request.getPassword() == null || !passwordPattern.matcher(request.getPassword()).matches()) {
            throw new IncorrectPatternException("Invalid password format");
        }

        Optional<UserModel> existing = findUserByEmailPort.findByEmail(request.getEmail());
        if (existing.isPresent()) {
            throw new UserExistsException("User already exists");
        }

        UserModel model = dtoMapper.dtoToModel(request);
        model.setCreated(LocalDateTime.now());
        model.setLastLogin(LocalDateTime.now());
        model.setId(UUID.randomUUID());
        model.setIsActive(Boolean.TRUE);

        model.setPasswordPlain(request.getPassword());
        model.setPasswordEncrypted(passwordEncoder.encode(request.getPassword()));

        String token = jwtService.generateToken(model.getEmail());
        model.setToken(token);

        UserModel saved = saveUserPort.apply(model);

        UserResponseDTO response = dtoMapper.modelToResponseDto(saved);
        response.setPassword(model.getPasswordEncrypted());
        return response;
    }
}
