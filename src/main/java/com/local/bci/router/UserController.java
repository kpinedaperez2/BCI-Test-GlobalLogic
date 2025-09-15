package com.local.bci.router;

import com.local.bci.application.dto.SignUpRequestDTO;
import com.local.bci.application.dto.UserResponseDTO;
import com.local.bci.application.usecase.LoginUseCase;
import com.local.bci.application.usecase.SingUpUseCase;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST controller for user operations: sign-up and login.
 * <p>
 * Exposes endpoints to register a new user and authenticate an existing user.
 * Accepts and returns JSON payloads.
 * </p>
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    private final SingUpUseCase signUpUseCase;
    private final LoginUseCase loginUseCase;

    public UserController(SingUpUseCase signUpUseCase, LoginUseCase loginUseCase) {
        this.signUpUseCase = signUpUseCase;
        this.loginUseCase = loginUseCase;
    }

    /**
     * Registers a new user.
     *
     * @param request the sign-up request containing user details
     * @return {@link ResponseEntity} with {@link UserResponseDTO} and HTTP status 201 Created
     */
    @PostMapping(path = "/sign-up", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponseDTO> signUp(@Valid @RequestBody SignUpRequestDTO request) {
        UserResponseDTO resp = signUpUseCase.apply(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    /**
     * Authenticates a user using a JWT token from the Authorization header.
     *
     * @param authHeader the Authorization header containing the Bearer token
     * @return {@link ResponseEntity} with {@link UserResponseDTO} and HTTP status 200 OK
     */
    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponseDTO> login(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        UserResponseDTO resp = loginUseCase.apply(token);
        return ResponseEntity.ok(resp);
    }
}
